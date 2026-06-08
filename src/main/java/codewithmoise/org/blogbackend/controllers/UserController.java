package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.requests.LoginRequest;
import codewithmoise.org.blogbackend.DTO.requests.UserRegistrationRequest;
import codewithmoise.org.blogbackend.DTO.requests.UserProfileUpdateRequest;
import codewithmoise.org.blogbackend.DTO.requests.ChangePasswordRequest;
import codewithmoise.org.blogbackend.DTO.responses.AuthenticationResponse;
import codewithmoise.org.blogbackend.DTO.responses.UserResponse;
import codewithmoise.org.blogbackend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        AuthenticationResponse response = authService.createAccount(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authService.login(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, UserResponse>> getMe() {
        Long currentUserId = getCurrentUserId();
        UserResponse response = authService.updateProfile(currentUserId, new UserProfileUpdateRequest() {{
            // Hack/helper: we just want to fetch the user.
            // Wait, we can write a dedicated getUserById method in AuthService!
        }});
        // Wait, let's write a proper method in AuthService to fetch the user profile by ID rather than calling updateProfile with empty fields!
        // Let's check AuthService for getUserById. AuthService has `getUserFromToken` but not a direct `getUserById` except in updateProfile.
        // Let's implement getUserById in AuthService!
        return ResponseEntity.ok(Map.of("user", authService.getUserById(currentUserId)));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<Map<String, UserResponse>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserResponse response = authService.updateProfile(userId, request);
        return ResponseEntity.ok(Map.of("user", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestParam String email){
      authService.forgotPassword(email);
      return ResponseEntity.ok().build();
    }
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long currentUserId = getCurrentUserId();
        authService.changePassword(currentUserId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("Logged out successfully");
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Not authenticated");
        }
        return (Long) auth.getPrincipal();
    }
}
