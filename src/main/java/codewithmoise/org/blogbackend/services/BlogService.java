package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.Tag;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.BlogRepository;
import codewithmoise.org.blogbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final TagService tagService;

    public BlogService(BlogRepository blogRepository, UserRepository userRepository, TagService tagService) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.tagService = tagService;
    }

   // Fetching in pages each has 10 blogs
    public Page<BlogResponse> getBlogs(int page, int size) {
      Pageable pageable = PageRequest.of(
                page, size
        );
         Page<Blog> blogs = blogRepository.findAllByOrderByCreatedAtDesc(pageable);
         return blogs.map(this::mapToBlogResponse);

    }

    public Optional<BlogResponse> getBlogById(Long id) {
        return blogRepository.findById(id).map(this::mapToBlogResponse);
    }

    public BlogResponse createBlog(BlogPostRequest blogPostRequest) {

        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Blog blog = new Blog();
        blog.setTitle(blogPostRequest.getTitle());
        blog.setContent(blogPostRequest.getContent());
        blog.setCategory(blogPostRequest.getCategory());
        blog.setUser(user);

        if (blogPostRequest.getTags() != null && !blogPostRequest.getTags().isEmpty()) {
            List<Tag> tags = tagService.findOrCreateTags(blogPostRequest.getTags());
            blog.setTags(tags);
        }

        Blog savedBlog = blogRepository.save(blog);
        return mapToBlogResponse(savedBlog);
    }

    public BlogResponse updateBlog(BlogUpdateRequest blogUpdateRequest, Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        Long currentUserId = getCurrentUserId();
        if(!blog.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can not update");
        }

        if (blogUpdateRequest.getContent() != null) {
            blog.setContent(blogUpdateRequest.getContent());
        }

        if (blogUpdateRequest.getTags() != null) {
            List<Tag> tags = tagService.findOrCreateTags(blogUpdateRequest.getTags());
            blog.setTags(tags);
        }

        Blog updatedBlog = blogRepository.save(blog);
        return mapToBlogResponse(updatedBlog);
    }

    public void deleteBlog(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        Long currentUserId = getCurrentUserId();
        if(!blog.getUser().getId().equals(currentUserId)){
            throw new AccessDeniedException("You can not delete");
        }
        blogRepository.delete(blog);
    }

    public List<BlogResponse> getBlogsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Blog> blogs = blogRepository.findByUser(user);
        return blogs.stream().map(this::mapToBlogResponse).toList();
    }

    private BlogResponse mapToBlogResponse(Blog blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setContent(blog.getContent());
        response.setCategory(blog.getCategory());
        response.setAuthorId(blog.getUser().getId());
        response.setCreatedAt(blog.getCreatedAt());
        
        if (blog.getTags() != null) {
            List<String> tagNames = blog.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList());
            response.setTags(tagNames);
        }
        
        return response;
    }

    private Long getCurrentUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");

        }
        return (Long) auth.getPrincipal();
    }
}
