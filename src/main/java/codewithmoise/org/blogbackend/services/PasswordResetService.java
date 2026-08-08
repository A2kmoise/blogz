package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.exception.PasswordResetException;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.UserRepository;
import codewithmoise.org.blogbackend.util.PasswordEncoder;
import codewithmoise.org.blogbackend.util.TokenGenerator;
import codewithmoise.org.blogbackend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Value("${password.reset.max.attempts:5}")
    private int maxResetAttempts;

    @Value("${password.reset.lockout.minutes:30}")
    private long lockoutDurationMinutes;

    @Value("${app.base-url:https://yourblog.com}")
    private String appBaseUrl;

    // Track failed attempts per email (key: email, value: [attempts, lockout_time])
    private final ConcurrentHashMap<String, FailureTracker> failureAttempts = new ConcurrentHashMap<>();

    /**
     * Track reset attempt failures
     */
    private static class FailureTracker {
        AtomicInteger attempts = new AtomicInteger(0);
        volatile LocalDateTime lockedUntil = null;
    }

    /**
     * Request password reset for a user
     * Generates a secure token and sends reset email
     * @param email User email address
     */
    @Transactional
    public void requestPasswordReset(String email) {
        // Input validation
        if (email == null || email.trim().isEmpty()) {
            throw new PasswordResetException("Email cannot be empty.");
        }

        // Sanitize email
        email = ValidationUtil.sanitizeEmail(email);

        // Validate email format
        if (!ValidationUtil.isValidEmail(email)) {
            log.warn("Invalid email format attempted: {}", email);
            throw new PasswordResetException("Please provide a valid email address.");
        }

        // Check for injection patterns
        if (ValidationUtil.containsSQLInjectionPatterns(email)) {
            log.warn("SQL injection pattern detected in email: {}", email);
            throw new PasswordResetException("Invalid email format.");
        }

        // Check rate limiting and brute force protection
        checkBruteForceProtection(email);

        String finalEmail = email;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    recordFailedAttempt(finalEmail);
                    log.warn("Password reset requested for non-existent email: {}", finalEmail);
                    // Return generic message (security: don't reveal if email exists)
                    throw new PasswordResetException("If an account exists with this email, you will receive a password reset link.");
                });

        // Rate limiting: prevent spam of reset requests
        if (isResetRequestTooSoon(user)) {
            recordFailedAttempt(email);
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

        // Clear failed attempts on success
        clearFailureTracker(email);

        log.info("Password reset token generated for user: {}", email);
    }

    /**
     * Verify the reset token is valid
     * @param token Reset token
     * @return User associated with token if valid
     */
    @Transactional(readOnly = true)
    public User verifyResetToken(String token) {
        // Validate token input
        if (token == null || token.trim().isEmpty()) {
            throw new PasswordResetException("Invalid reset token.");
        }

        token = token.trim();

        // Validate token format
        if (!ValidationUtil.isValidToken(token)) {
            log.warn("Invalid token format attempted");
            throw new PasswordResetException("Invalid reset token.");
        }

        // Check for injection patterns
        if (ValidationUtil.containsSQLInjectionPatterns(token)) {
            log.warn("SQL injection pattern detected in token");
            throw new PasswordResetException("Invalid reset token.");
        }

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid password reset token attempted");
                    throw new PasswordResetException("Invalid or expired reset token.");
                });

        // Check if token has expired
        if (user.getPasswordResetTokenExpiryDate() == null ||
                user.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
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
        // Input validation
        if (token == null || token.trim().isEmpty()) {
            throw new PasswordResetException("Reset token is required.");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            throw new PasswordResetException("New password is required.");
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new PasswordResetException("Password confirmation is required.");
        }

        // Validate inputs length
        if (!ValidationUtil.isValidLength(newPassword, 128)) {
            throw new PasswordResetException("Password is too long.");
        }

        // Verify token first
        User user = verifyResetToken(token.trim());

        // Validate password match
        if (!ValidationUtil.passwordsMatch(newPassword, confirmPassword)) {
            recordFailedAttempt(user.getEmail());
            throw new PasswordResetException("Passwords do not match.");
        }

        // Validate password strength
        ValidationUtil.PasswordValidationResult validationResult = ValidationUtil.validatePassword(newPassword);
        if (!validationResult.isValid()) {
            recordFailedAttempt(user.getEmail());
            throw new PasswordResetException(validationResult.getErrors());
        }

        // Ensure new password is different from current
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            recordFailedAttempt(user.getEmail());
            throw new PasswordResetException("New password must be different from current password.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear reset token
        clearResetToken(user);

        userRepository.save(user);

        // Clear failed attempts on success
        clearFailureTracker(user.getEmail());

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
        ValidationUtil.PasswordValidationResult result = ValidationUtil.validatePassword(password);
        if (!result.isValid()) {
            throw new PasswordResetException(result.getErrors());
        }
    }

    /**
     * Check brute force protection - prevent repeated failed attempts
     */
    private void checkBruteForceProtection(String email) {
        FailureTracker tracker = failureAttempts.get(email);

        if (tracker != null && tracker.lockedUntil != null) {
            if (tracker.lockedUntil.isAfter(LocalDateTime.now())) {
                log.warn("Account temporarily locked due to too many reset attempts: {}", email);
                throw new PasswordResetException("Too many reset attempts. Please try again later.");
            } else {
                // Lockout period expired, reset attempts
                tracker.attempts.set(0);
                tracker.lockedUntil = null;
            }
        }
    }

    /**
     * Record failed reset attempt for brute force protection
     */
    private void recordFailedAttempt(String email) {
        FailureTracker tracker = failureAttempts.computeIfAbsent(email, k -> new FailureTracker());
        int attempts = tracker.attempts.incrementAndGet();

        if (attempts >= maxResetAttempts) {
            tracker.lockedUntil = LocalDateTime.now().plusMinutes(lockoutDurationMinutes);
            log.warn("Account locked due to too many failed reset attempts: {} (attempts: {})", email, attempts);
        }
    }

    /**
     * Clear failure tracker for successful operations
     */
    private void clearFailureTracker(String email) {
        failureAttempts.remove(email);
    }
}
