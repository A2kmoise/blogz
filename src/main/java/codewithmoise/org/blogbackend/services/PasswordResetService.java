package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.exception.PasswordResetException;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.UserRepository;
import codewithmoise.org.blogbackend.util.PasswordEncoder;
import codewithmoise.org.blogbackend.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${password.reset.token.expiry.minutes:24}")
    private long resetTokenExpiryMinutes;

    @Value("${password.reset.request.cooldown.minutes:1}")
    private long resetRequestCooldownMinutes;

    @Value("${app.base-url:https://yourblog.com}")
    private String appBaseUrl;

    /**
     * Request password reset for a user
     * Generates a secure token and sends reset email
     * @param email User email address
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset requested for non-existent email: {}", email);
                    throw new PasswordResetException("If an account exists with this email, you will receive a password reset link.");
                });

        // Rate limiting: prevent spam of reset requests
        if (isResetRequestTooSoon(user)) {
            log.warn("Password reset requested too soon for user: {}", email);
            throw new PasswordResetException("Please wait before requesting another password reset.");
        }

        // Generate secure token
        String resetToken = TokenGenerator.generateSecureToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes);

        // Update user with reset token
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiryDate(expiryTime);
        user.setPasswordResetRequested(true);
        user.setLastPasswordResetRequestTime(LocalDateTime.now());

        userRepository.save(user);

        // Send reset email
        String resetLink = buildResetLink(resetToken);
        emailService.sendPasswordResetEmail(email, resetLink, user.getUsername());

        log.info("Password reset token generated for user: {}", email);
    }

    /**
     * Verify the reset token is valid
     * @param token Reset token
     * @return User associated with token if valid
     */
    @Transactional(readOnly = true)
    public User verifyResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new PasswordResetException("Invalid reset token.");
        }

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid password reset token attempted: {}", token);
                    throw new PasswordResetException("Invalid or expired reset token.");
                });

        // Check if token has expired
        if (user.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Password reset token expired for user: {}", user.getEmail());
            // Clear expired token
            clearResetToken(user);
            throw new PasswordResetException("Reset token has expired. Please request a new one.");
        }

        if (!user.isPasswordResetRequested()) {
            log.warn("Password reset requested but reset not marked as requested for user: {}", user.getEmail());
            throw new PasswordResetException("Invalid reset token.");
        }

        return user;
    }

    /**
     * Reset password with valid token
     * @param token Reset token
     * @param newPassword New password
     * @param confirmPassword Confirm password
     */
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        // Verify token first
        User user = verifyResetToken(token);

        // Validate password match
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordResetException("Passwords do not match.");
        }

        // Validate password strength
        validatePasswordStrength(newPassword);

        // Ensure new password is different from current
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new PasswordResetException("New password must be different from current password.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear reset token
        clearResetToken(user);

        userRepository.save(user);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    /**
     * Check if reset request is too soon (rate limiting)
     */
    private boolean isResetRequestTooSoon(User user) {
        if (user.getLastPasswordResetRequestTime() == null) {
            return false;
        }
        return user.getLastPasswordResetRequestTime()
                .plusMinutes(resetRequestCooldownMinutes)
                .isAfter(LocalDateTime.now());
    }

    /**
     * Clear reset token from user
     */
    @Transactional
    public void clearResetToken(User user) {
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiryDate(null);
        user.setPasswordResetRequested(false);
        userRepository.save(user);
    }

    /**
     * Build reset link URL
     */
    private String buildResetLink(String token) {
        return String.format("%s/reset-password?token=%s", appBaseUrl, token);
    }

    /**
     * Validate password strength
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new PasswordResetException("Password must be at least 8 characters long.");
        }

        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~].*");

        if (!hasUpperCase || !hasLowerCase || !hasDigit) {
            throw new PasswordResetException(
                    "Password must contain uppercase letters, lowercase letters, and numbers."
            );
        }
    }
}
