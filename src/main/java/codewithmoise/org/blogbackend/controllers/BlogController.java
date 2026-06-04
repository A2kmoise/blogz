package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.DTO.responses.PaginatedResponse;
import codewithmoise.org.blogbackend.enums.BlogCategory;
import codewithmoise.org.blogbackend.services.BlogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
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
    public ResponseEntity<PaginatedResponse<BlogResponse>> getBlogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Boolean published
    ) {
        int finalLimit = (pageSize != null) ? pageSize : (limit != null ? limit : 10);
        // sort param currently not used; could be implemented later
        return ResponseEntity.ok(blogService.getBlogs(page, finalLimit, search, category, tag, sort, authorId, published));
    }

    @GetMapping("/mine")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<BlogResponse>> getMyBlogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(blogService.getMyBlogs(page, limit));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        Optional<BlogResponse> blog = blogService.getBlogById(id);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BlogResponse> getBlogBySlug(@PathVariable String slug) {
        Optional<BlogResponse> blog = blogService.getBlogBySlug(slug);
        return blog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<PaginatedResponse<BlogResponse>> getBlogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(blogService.getBlogsByUser(userId, page, limit));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponse> createBlog(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "excerpt", required = false) String excerpt,
            @RequestParam(value = "published", required = false) Boolean published,
            @RequestParam(value = "scheduledAt", required = false) String scheduledAt,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        BlogPostRequest request = new BlogPostRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setCategory(BlogCategory.valueOf(category.toUpperCase()));
        request.setExcerpt(excerpt);
        request.setPublished(published);
        request.setImage(image);
        
        if (tags != null && !tags.isEmpty()) {
            request.setTags(Arrays.asList(tags.split(",")));
        }

        if (scheduledAt != null && !scheduledAt.isEmpty()){
            request.setScheduledAt(LocalDateTime.parse(scheduledAt));
        }
        
        BlogResponse createdBlog = blogService.createBlog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBlog);
    }

    @PatchMapping("/{id}")
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