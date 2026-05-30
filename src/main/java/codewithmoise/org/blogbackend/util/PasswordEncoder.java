package codewithmoise.org.blogbackend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder;

    public PasswordEncoder() {
        this.encoder = new BCryptPasswordEncoder();
    }

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public boolean isEncoded(String password) {
        // BCrypt hashes always start with $2a$, $2b$, $2x$, or $2y$
        return password != null && password.matches("^\\$2[abxy]\\$\\d{2}\\$.{53}$");
    }
}