package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.requests.ForgotPasswordRequest;
import codewithmoise.org.blogbackend.DTO.requests.ResetPasswordRequest;
import codewithmoise.org.blogbackend.DTO.requests.VerifyResetTokenRequest;
import codewithmoise.org.blogbackend.exception.PasswordResetException;
import codewithmoise.org.blogbackend.services.PasswordResetService;
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
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Email is required", false, null));
            }

            passwordResetService.requestPasswordReset(request.getEmail());

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
            if (request.getToken() == null || request.getToken().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Token is required", false, null));
            }

            passwordResetService.verifyResetToken(request.getToken());

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
            // Validate request
            if (request.getToken() == null || request.getToken().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Token is required", false, null));
            }

            if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("New password is required", false, null));
            }

            if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(createResponse("Password confirmation is required", false, null));
            }

            // Reset password
            passwordResetService.resetPassword(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );

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
