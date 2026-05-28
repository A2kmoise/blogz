package codewithmoise.org.blogbackend.config;

import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.UserRepository;
import codewithmoise.org.blogbackend.util.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@blog.com")) {
            User admin = new User();
            admin.setEmail("admin@blog.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRoles.ADMIN);
            userRepository.save(admin);
        }
    }
}