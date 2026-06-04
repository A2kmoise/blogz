package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.responses.AdminStatsResponse;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.DTO.responses.PaginatedResponse;
import codewithmoise.org.blogbackend.DTO.responses.UserResponse;
import codewithmoise.org.blogbackend.enums.BlogCategory;
import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.Tag;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.BlogRepository;
import codewithmoise.org.blogbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    public AdminService(BlogRepository blogRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    // ─── Stats ───────────────────────────────────────────────
    public AdminStatsResponse getStats() {
        long totalBlogs = blogRepository.count();
        long totalAuthors = userRepository.countByRole(UserRoles.AUTHOR);
        long totalAdmins = userRepository.countByRole(UserRoles.ADMIN);

        Pageable latestSix = PageRequest.of(0, 6, Sort.by("createdAt").descending());
        Page<Blog> recentBlogs = blogRepository.findAll(latestSix);
        
        List<AdminStatsResponse.ActivityResponse> recentActivity = recentBlogs.getContent().stream()
                .map(blog -> {
                    String authorName = blog.getUser() != null ? blog.getUser().getUsername() : "Unknown";
                    String text = authorName + " published \"" + blog.getTitle() + "\"";
                    String date = blog.getCreatedAt() != null ? blog.getCreatedAt().toString() : "";
                    return new AdminStatsResponse.ActivityResponse(blog.getId().toString(), text, date);
                })
                .collect(Collectors.toList());

        return new AdminStatsResponse(totalBlogs, totalAuthors, totalAdmins, recentActivity);
    }

    // ─── Users ───────────────────────────────────────────────
    public List<UserResponse> getAllAuthors(String search) {
        return userRepository.findByRoleAndSearch(UserRoles.AUTHOR, search)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    public UserResponse toggleSuspendUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRoles.ADMIN) {
            throw new AccessDeniedException("Cannot suspend an admin");
        }
        user.setSuspended(!user.isSuspended());
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    // ─── Blogs ───────────────────────────────────────────────
    public PaginatedResponse<BlogResponse> getAllBlogs(String search, String category, int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("createdAt").descending());

        BlogCategory blogCategory = null;
        if (category != null && !category.isEmpty()) {
            for (BlogCategory val : BlogCategory.values()) {
                if (val.name().equalsIgnoreCase(category)) {
                    blogCategory = val;
                    break;
                }
            }
        }

        Page<Blog> blogs = blogRepository.findBlogsWithFilters(null, null, blogCategory, null, search, pageable);
        List<BlogResponse> mappedItems = blogs.getContent().stream()
                .map(this::mapToBlogResponse)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(mappedItems, blogs.getTotalElements(), page, size);
    }

    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        blogRepository.delete(blog);
    }

    // ─── Mappers ─────────────────────────────────────────────
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getUsername());
        response.setRole(UserRoles.valueOf(user.getRole().name()));
        response.setSuspended(user.isSuspended());
        response.setCreatedAt(user.getCreatedAt());
        return response;
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
            response.setTags(blog.getTags().stream().map(Tag::getName).toList());
        }
        return response;
    }
}