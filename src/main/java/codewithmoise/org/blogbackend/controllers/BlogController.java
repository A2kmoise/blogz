package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.services.BlogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin(origins = "*")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public ResponseEntity<Page<BlogResponse>> getBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(blogService.getBlogs(page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        Optional<BlogResponse> blog = blogService.getBlogById(id);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BlogResponse>> getBlogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(blogService.getBlogsByUser(userId));
    }

    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(@Valid @RequestBody BlogPostRequest blog) {
        BlogResponse createdBlog = blogService.createBlog(blog);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBlog);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(
            @Valid @RequestBody BlogUpdateRequest blogUpdateRequest,
            @PathVariable Long id) {
        BlogResponse updatedBlog = blogService.updateBlog(blogUpdateRequest, id);
        return ResponseEntity.ok(updatedBlog);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
       return ResponseEntity.noContent().build();
    }
}