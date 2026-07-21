package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.requests.ForgotPasswordRequest;
import codewithmoise.org.blogbackend.DTO.requests.ResetPasswordRequest;
import codewithmoise.org.blogbackend.DTO.requests.VerifyResetTokenRequest;
import codewithmoise.org.blogbackend.exception.PasswordResetException;
import codewithmoise.org.blogbackend.services.PasswordResetService;
import codewithmoise.org.blogbackend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Request password reset
     * POST /api/auth/password-reset/request
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@RequestBody ForgotPasswordRequest request) {
        try {
            // Input validation
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Request body is required", false, null));
            }

            String email = request.getEmail();

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Email is required", false, null));
            }

            // Sanitize email
            email = ValidationUtil.sanitizeEmail(email);

            // Validate email format
            if (!ValidationUtil.isValidEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Please provide a valid email address", false, null));
            }

            // Validate email length
            if (!ValidationUtil.isEmailLengthValid(email)) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Email address is too long", false, null));
            }

            // Check for SQL injection patterns
            if (ValidationUtil.containsSQLInjectionPatterns(email)) {
                log.warn("Potential SQL injection attempted on password reset endpoint with email: {}", email);
                return ResponseEntity.badRequest()
                        .body(createResponse("Invalid email format", false, null));
            }

            passwordResetService.requestPasswordReset(email);

            Map<String, Object> response = createResponse(
                    "If an account exists with this email, a password reset link has been sent.",
                    true,
                    null
            );
            return ResponseEntity.ok(response);

        } catch (PasswordResetException e) {
            log.warn("Password reset request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Unexpected error during password reset request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse("An error occurred. Please try again later.", false, null));
        }
    }

    /**
     * Verify reset token validity
     * POST /api/auth/password-reset/verify-token
     */
    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyResetToken(@RequestBody VerifyResetTokenRequest request) {
        try {
            // Input validation
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Request body is required", false, null));
            }

            String token = request.getToken();

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Token is required", false, null));
            }

            // Sanitize token
            token = ValidationUtil.sanitizeInput(token.trim());

            // Validate token format
            if (!ValidationUtil.isValidToken(token)) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Invalid token format", false, null));
            }

            // Check for injection patterns
            if (ValidationUtil.containsSQLInjectionPatterns(token)) {
                log.warn("Potential SQL injection attempted on token verification");
                return ResponseEntity.badRequest()
                        .body(createResponse("Invalid token format", false, null));
            }

            passwordResetService.verifyResetToken(token);

            Map<String, Object> response = createResponse(
                    "Token is valid",
                    true,
                    null
            );
            return ResponseEntity.ok(response);

        } catch (PasswordResetException e) {
            log.warn("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Unexpected error during token verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse("An error occurred. Please try again later.", false, null));
        }
    }

    /**
     * Reset password with valid token
     * POST /api/auth/password-reset/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // Input validation
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Request body is required", false, null));
            }

            String token = request.getToken();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();

            // Validate token
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Token is required", false, null));
            }

            // Validate new password
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("New password is required", false, null));
            }

            if (newPassword.length() > 128) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Password is too long", false, null));
            }

            // Validate password confirmation
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Password confirmation is required", false, null));
            }

            if (confirmPassword.length() > 128) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Password confirmation is too long", false, null));
            }

            // Sanitize inputs
            token = ValidationUtil.sanitizeInput(token.trim());

            // Validate token format
            if (!ValidationUtil.isValidToken(token)) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Invalid token format", false, null));
            }

            // Check for injection patterns
            if (ValidationUtil.containsSQLInjectionPatterns(token)) {
                log.warn("Potential SQL injection attempted on password reset");
                return ResponseEntity.badRequest()
                        .body(createResponse("Invalid token format", false, null));
            }

            // Pre-validate passwords before sending to service
            ValidationUtil.PasswordValidationResult validationResult = ValidationUtil.validatePassword(newPassword);
            if (!validationResult.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createResponse(validationResult.getErrors(), false, null));
            }

            // Reset password
            passwordResetService.resetPassword(token, newPassword, confirmPassword);

            Map<String, Object> response = createResponse(
                    "Password has been reset successfully. You can now login with your new password.",
                    true,
                    null
            );
            return ResponseEntity.ok(response);

        } catch (PasswordResetException e) {
            log.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Unexpected error during password reset", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse("An error occurred. Please try again later.", false, null));
        }
    }

    /**
     * Helper method to create response structure
     */
    private Map<String, Object> createResponse(String message, boolean success, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("success", success);
        response.put("data", data);
        return response;
    }
}
