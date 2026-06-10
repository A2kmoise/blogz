package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.DTO.responses.PaginatedResponse;
import codewithmoise.org.blogbackend.enums.BlogCategory;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.Tag;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.BlogRepository;
import codewithmoise.org.blogbackend.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final CloudinaryService cloudinaryService;

    public BlogService(BlogRepository blogRepository, UserRepository userRepository, TagService tagService, CloudinaryService cloudinaryService) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.tagService = tagService;
        this.cloudinaryService = cloudinaryService;
    }

    @Cacheable(value = "blogs", key = "#page + '-' + #size + '-' + #category + '-' + #search")
    @Transactional(readOnly = true)
    public PaginatedResponse<BlogResponse> getBlogs(int page, int size, String search, String category, String tag, String sort, Long authorId, Boolean published) {
        int pageIndex = page > 0 ? page - 1 : 0;

        Sort sorting;
        if ("oldest".equalsIgnoreCase(sort)) {
            sorting = Sort.by("createdAt").ascending();
        } else {
            sorting = Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(pageIndex, size, sorting);

        BlogCategory blogCategory = null;
        if (category != null && !category.isEmpty()) {
            for (BlogCategory val : BlogCategory.values()) {
                if (val.name().equalsIgnoreCase(category)) {
                    blogCategory = val;
                    break;
                }
            }
        }

        Page<Blog> blogs = blogRepository.findBlogsWithFilters(published, authorId, blogCategory, tag, search, pageable);
        List<BlogResponse> mappedItems = blogs.getContent().stream()
                .map(this::mapToBlogResponse)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(mappedItems, blogs.getTotalElements(), page, size);
    }

    @Cacheable(value = "blog", key = "'id-' + #id")
    @Transactional(readOnly = true)
    public Optional<BlogResponse> getBlogById(Long id) {
        return blogRepository.findById(id).map(this::mapToBlogResponse);
    }

    @Cacheable(value = "blog", key = "#slug")
    @Transactional(readOnly = true)
    public Optional<BlogResponse> getBlogBySlug(String slug) {
        return blogRepository.findBySlug(slug).map(blog -> {
            blog.setViews(blog.getViews() + 1);
            blogRepository.save(blog);
            return this.mapToBlogResponse(blog);
        });
    }

    @CacheEvict(value = "blogs", allEntries = true)
    public BlogResponse createBlog(BlogPostRequest blogPostRequest) {
        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Blog blog = new Blog();
        blog.setTitle(blogPostRequest.getTitle());
        blog.setContent(blogPostRequest.getContent());
        blog.setCategory(blogPostRequest.getCategory());
        blog.setUser(user);

        // Generate unique slug
        String baseSlug = blogPostRequest.getTitle().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (baseSlug.isEmpty()) {
            baseSlug = "post";
        }
        String slug = baseSlug + "-" + System.currentTimeMillis();
        blog.setSlug(slug);

        // Excerpt
        if (blogPostRequest.getExcerpt() != null && !blogPostRequest.getExcerpt().isEmpty()) {
            blog.setExcerpt(blogPostRequest.getExcerpt());
        } else {
            String plainText = blogPostRequest.getContent().replaceAll("<[^>]+>", "");
            blog.setExcerpt(plainText.length() > 160 ? plainText.substring(0, 160) : plainText);
        }

        // Published
        if (blogPostRequest.getScheduledAt() != null) {
            blog.setPublished(false);
            blog.setScheduledAt(blogPostRequest.getScheduledAt());
        } else if (blogPostRequest.getPublished() != null) {
            blog.setPublished(blogPostRequest.getPublished());
        } else {
            blog.setPublished(true);
        }

        blog.setViews(0);

        // Handle image upload
        if (blogPostRequest.getImage() != null && !blogPostRequest.getImage().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(blogPostRequest.getImage());
                blog.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        if (blogPostRequest.getTags() != null && !blogPostRequest.getTags().isEmpty()) {
            List<Tag> tags = tagService.findOrCreateTags(blogPostRequest.getTags());
            blog.setTags(tags);
        }

        Blog savedBlog = blogRepository.save(blog);
        return mapToBlogResponse(savedBlog);
    }

    @Caching(evict = {
            @CacheEvict(value = "blog",  key = "#blogId"),
            @CacheEvict(value = "blogs", allEntries = true)
    })
    public BlogResponse updateBlog(BlogUpdateRequest blogUpdateRequest, Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        Long currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!blog.getUser().getId().equals(currentUserId) && currentUser.getRole() != codewithmoise.org.blogbackend.enums.UserRoles.ADMIN) {
            throw new AccessDeniedException("You can not update this blog");
        }

        if (blogUpdateRequest.getTitle() != null) {
            blog.setTitle(blogUpdateRequest.getTitle());
        }

        if (blogUpdateRequest.getContent() != null) {
            blog.setContent(blogUpdateRequest.getContent());
        }

        if (blogUpdateRequest.getExcerpt() != null) {
            blog.setExcerpt(blogUpdateRequest.getExcerpt());
        } else if (blogUpdateRequest.getContent() != null) {
            String plainText = blogUpdateRequest.getContent().replaceAll("<[^>]+>", "");
            blog.setExcerpt(plainText.length() > 160 ? plainText.substring(0, 160) : plainText);
        }

        if (blogUpdateRequest.getCoverImage() != null) {
            blog.setImageUrl(blogUpdateRequest.getCoverImage());
        }

        if (blogUpdateRequest.getCategoryId() != null) {
            for (BlogCategory val : BlogCategory.values()) {
                if (val.name().equalsIgnoreCase(blogUpdateRequest.getCategoryId())) {
                    blog.setCategory(val);
                    break;
                }
            }
        }

        if (blogUpdateRequest.getPublished() != null) {
            blog.setPublished(blogUpdateRequest.getPublished());
            if (blogUpdateRequest.getPublished()) {
                blog.setScheduledAt(null);
            }
        }

        if (blogUpdateRequest.getScheduledAt() != null) {
            String schedStr = blogUpdateRequest.getScheduledAt();
            if (schedStr.isEmpty() || "null".equalsIgnoreCase(schedStr)) {
                blog.setScheduledAt(null);
            } else {
                try {
                    if (schedStr.endsWith("Z") || schedStr.contains("+") || (schedStr.length() > 10 && schedStr.substring(10).contains("-"))) {
                        blog.setScheduledAt(java.time.OffsetDateTime.parse(schedStr).toLocalDateTime());
                    } else {
                        blog.setScheduledAt(java.time.LocalDateTime.parse(schedStr));
                    }
                } catch (Exception e) {
                    try {
                        blog.setScheduledAt(java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse(schedStr, java.time.LocalDateTime::from));
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Invalid date format for scheduledAt: " + schedStr);
                    }
                }
            }
        }

        if (blogUpdateRequest.getTags() != null) {
            List<Tag> tags = tagService.findOrCreateTags(blogUpdateRequest.getTags());
            blog.setTags(tags);
        }

        Blog updatedBlog = blogRepository.save(blog);
        return mapToBlogResponse(updatedBlog);
    }

    @Caching(evict = {
            @CacheEvict(value = "blog",  key = "#blogId"),
            @CacheEvict(value = "blogs", allEntries = true)
    })
    public void deleteBlog(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        Long currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!blog.getUser().getId().equals(currentUserId) && currentUser.getRole() != codewithmoise.org.blogbackend.enums.UserRoles.ADMIN) {
            throw new AccessDeniedException("You can not delete this blog");
        }

        if (blog.getImageUrl() != null && !blog.getImageUrl().isEmpty()) {
            try {
                cloudinaryService.deleteImage(blog.getImageUrl());
            } catch (Exception e) {
                System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
            }
        }

        blogRepository.delete(blog);
    }

    public PaginatedResponse<BlogResponse> getBlogsByUser(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return getBlogs(page, size, null, null, null, "newest", userId, Boolean.TRUE);
    }

    public PaginatedResponse<BlogResponse> getMyBlogs(int page, int size) {
        Long currentUserId = getCurrentUserId();
        return getBlogs(page, size, null, null, null, "newest", currentUserId, null);
    }

    private BlogResponse mapToBlogResponse(Blog blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setSlug(blog.getSlug());
        response.setTitle(blog.getTitle());
        response.setExcerpt(blog.getExcerpt());
        response.setContent(blog.getContent());
        response.setCoverImage(blog.getImageUrl());
        response.setCreatedAt(blog.getCreatedAt());
        response.setUpdatedAt(blog.getUpdatedAt());
        response.setScheduledAt(blog.getScheduledAt());
        response.setViews(blog.getViews());
        response.setPublished(blog.isPublished());

        if (blog.getCategory() != null) {
            String catName = blog.getCategory().name();
            String catSlug = catName.toLowerCase();
            response.setCategory(new BlogResponse.CategoryResponse(catName, catName, catSlug));
        }

        if (blog.getUser() != null) {
            response.setAuthor(new BlogResponse.AuthorResponse(
                    blog.getUser().getId(),
                    blog.getUser().getUsername(),
                    blog.getUser().getEmail()
            ));
        }

        if (blog.getTags() != null) {
            List<String> tagNames = blog.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList());
            response.setTags(tagNames);
        }

        return response;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Not authenticated");
        }
        return (Long) auth.getPrincipal();
    }
}
