package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.responses.AdminStatsResponse;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.DTO.responses.PaginatedResponse;
import codewithmoise.org.blogbackend.DTO.responses.UserResponse;
import codewithmoise.org.blogbackend.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ─── Stats ───────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ─── Users ───────────────────────────────────────────────
    @GetMapping("/authors")
    public ResponseEntity<List<UserResponse>> getAllAuthors(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getAllAuthors(search));
    }

    @PatchMapping("/authors/{userId}/suspend")
    public ResponseEntity<UserResponse> toggleSuspendUser(@PathVariable Long userId) {
        UserResponse response = adminService.toggleSuspendUser(userId);
        return ResponseEntity.ok(response);
    }

    // ─── Blogs ───────────────────────────────────────────────
    @GetMapping("/blogs")
    public ResponseEntity<PaginatedResponse<BlogResponse>> getAllBlogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(adminService.getAllBlogs(search, category, page, limit));
    }

    @DeleteMapping("/blogs/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        adminService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}