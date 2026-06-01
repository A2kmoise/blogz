package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.DTO.responses.UserResponse;
import codewithmoise.org.blogbackend.DTO.responses.AdminStatsResponse;
import codewithmoise.org.blogbackend.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
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
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/authors")
    public ResponseEntity<List<UserResponse>> getAllAuthors() {
        return ResponseEntity.ok(adminService.getAllAuthors());
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable Long id) {
        adminService.suspendUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/unsuspend")
    public ResponseEntity<Void> unsuspendUser(@PathVariable Long id) {
        adminService.unsuspendUser(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Blogs ───────────────────────────────────────────────
    @GetMapping("/blogs")
    public ResponseEntity<List<BlogResponse>> getAllBlogs() {
        return ResponseEntity.ok(adminService.getAllBlogs());
    }

    @DeleteMapping("/blogs/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        adminService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Categories ──────────────────────────────────────────
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(adminService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Void> createCategory(@RequestParam String name) {
        adminService.createCategory(name);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}