package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.requests.AuthenticationRequest;
import codewithmoise.org.blogbackend.DTO.requests.UserRegistrationRequest;
import codewithmoise.org.blogbackend.DTO.responses.AuthenticationResponse;
import codewithmoise.org.blogbackend.DTO.responses.UserResponse;
import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.UserRepository;
import codewithmoise.org.blogbackend.util.JwtUtil;
import codewithmoise.org.blogbackend.util.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthenticationResponse createAccount(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthenticationResponse("Email already exists", false);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRoles.AUTHOR);

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(
            savedUser.getEmail(), 
            savedUser.getId(), 
            savedUser.getRole().toString()
        );

        UserResponse userResponse = mapToUserResponse(savedUser);
        return new AuthenticationResponse("Account created successfully", userResponse, token, true);
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return new AuthenticationResponse("User not found", false);
        }

        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthenticationResponse("Invalid credentials", false);
        }

        String token = jwtUtil.generateToken(
            user.getEmail(), 
            user.getId(), 
            user.getRole().toString()
        );

        UserResponse userResponse = mapToUserResponse(user);
        return new AuthenticationResponse("Login successful", userResponse, token, true);
    }

    public UserResponse updateProfile(Long userId, UserRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }

    public UserResponse getUserFromToken(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToUserResponse(user);
    }

    public void logout() {
        // In a real application, you might want to blacklist the JWT token
        // For now, the client will simply remove the token from storage
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
