package codewithmoise.org.blogbackend.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32; // 256-bit token

    /**
     * Generate a cryptographically secure random token
     * @return Base64 encoded token
     */

    public static String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
