package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.responses.AdminStatsResponse;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.DTO.responses.UserResponse;
import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.Tag;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.BlogRepository;
import codewithmoise.org.blogbackend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

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
        long totalAuthors = userRepository.countByRole(UserRoles.AUTHOR);
        long totalBlogs = blogRepository.count();
        long suspendedUsers = userRepository.countBySuspended(true);

        return new AdminStatsResponse(totalAuthors, totalBlogs, suspendedUsers);
    }

    // ─── Users ───────────────────────────────────────────────
    public List<UserResponse> getAllAuthors() {
        return userRepository.findByRole(UserRoles.AUTHOR)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    public void suspendUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRoles.ADMIN) {
            throw new AccessDeniedException("Cannot suspend an admin");
        }
        user.setSuspended(true);
        userRepository.save(user);
    }

    public void unsuspendUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setSuspended(false);
        userRepository.save(user);
    }

    // ─── Blogs ───────────────────────────────────────────────
    public List<BlogResponse> getAllBlogs() {
        return blogRepository.findAll()
                .stream()
                .map(this::mapToBlogResponse)
                .toList();
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
        response.setRole(UserRoles.valueOf(user.getRole().name()));
        response.setSuspended(user.isSuspended());
        response.setCreatedAt(user.getCreatedAt());
        return response;
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
            response.setTags(blog.getTags().stream().map(Tag::getName).toList());
        }
        return response;
    }
}