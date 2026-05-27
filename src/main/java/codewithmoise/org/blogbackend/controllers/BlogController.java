package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.services.BlogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;
    private final ResourceTransactionManager resourceTransactionManager;

    public BlogController(BlogService blogService, ResourceTransactionManager resourceTransactionManager){
        this.blogService = blogService;
        this.resourceTransactionManager = resourceTransactionManager;
    }
    @GetMapping("/")
    public ResponseEntity<BlogResponse> getBlogs(){
        return ResponseEntity.ok().body(blogService.getBlogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Blog>> getBlogById(@PathVariable Long id){
        return ResponseEntity.ok().body(blogService.getBlogById(id));
    }

    @PostMapping("/")
    public ResponseEntity<BlogResponse> createBlog(@Valid  @RequestBody BlogPostRequest blog){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(blogService.createBlog(blog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(
            @Valid @RequestBody BlogUpdateRequest blogUpdateRequest,
            @PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(blogService.updateBlog(blogUpdateRequest, id));
    }

    @DeleteMapping("/{id}")
    public void deleteBlog(@PathVariable Long id){
      blogService.deleteBlog(id);
    }

}