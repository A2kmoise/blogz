package codewithmoise.org.blogbackend.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Email validation pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Password regex patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(
            "[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~]"
    );

    // Token validation
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate email length (max 255 chars)
     */
    public static boolean isEmailLengthValid(String email) {
        return email != null && email.length() <= 255;
    }

    /**
     * Sanitize email (trim and lowercase)
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Validate password meets all requirements
     */
    public static PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (password == null || password.isEmpty()) {
            result.addError("Password cannot be empty");
            return result;
        }

        if (password.length() < 8) {
            result.addError("Password must be at least 8 characters long");
        }

        if (password.length() > 128) {
            result.addError("Password must not exceed 128 characters");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            result.addError("Password must contain at least one uppercase letter (A-Z)");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            result.addError("Password must contain at least one lowercase letter (a-z)");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            result.addError("Password must contain at least one digit (0-9)");
        }

        // Check for common weak passwords
        if (isCommonWeakPassword(password)) {
            result.addError("Password is too common. Please use a stronger password");
        }

        // Check for sequential characters (like 123, abc, etc.)
        if (hasSequentialCharacters(password)) {
            result.addWarning("Password contains sequential characters - consider using a stronger password");
        }

        return result;
    }

    /**
     * Validate password confirmation matches
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Validate token format and length
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Token should be base64 URL-safe format
        if (token.length() < 32 || token.length() > 256) {
            return false;
        }

        return TOKEN_PATTERN.matcher(token).matches();
    }

    /**
     * Check for common weak passwords
     */
    private static boolean isCommonWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();
        String[] commonPasswords = {
                "password", "123456", "qwerty", "admin", "letmein",
                "welcome", "monkey", "dragon", "master", "sunshine",
                "princess", "football", "batman", "iloveyou", "trustno1",
                "password123", "admin123", "hello123", "test123", "123123"
        };

        for (String common : commonPasswords) {
            if (lowerPassword.equals(common) || lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for sequential characters (abc, 123, etc.)
     */
    private static boolean hasSequentialCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            // Check if 3+ consecutive characters in sequence
            if ((c2 == c1 + 1 && c3 == c2 + 1) ||
                    (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate input length to prevent buffer overflow
     */
    public static boolean isValidLength(String input, int maxLength) {
        return input != null && input.length() <= maxLength;
    }

    /**
     * Sanitize input to prevent injection attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }

    /**
     * Check for SQL injection patterns
     */
    public static boolean containsSQLInjectionPatterns(String input) {
        if (input == null) {
            return false;
        }
        String lowerInput = input.toLowerCase();
        String[] sqlPatterns = {
                "union", "select", "insert", "update", "delete", "drop",
                "create", "alter", "exec", "execute", "script", "javascript"
        };

        for (String pattern : sqlPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Result class for password validation
     */
    public static class PasswordValidationResult {
        private StringBuilder errors = new StringBuilder();
        private StringBuilder warnings = new StringBuilder();
        private boolean valid = true;

        public void addError(String error) {
            this.valid = false;
            if (errors.length() > 0) {
                errors.append("; ");
            }
            errors.append(error);
        }

        public void addWarning(String warning) {
            if (warnings.length() > 0) {
                warnings.append("; ");
            }
            warnings.append(warning);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrors() {
            return errors.toString();
        }

        public String getWarnings() {
            return warnings.toString();
        }

        public boolean hasWarnings() {
            return warnings.length() > 0;
        }
    }
}
