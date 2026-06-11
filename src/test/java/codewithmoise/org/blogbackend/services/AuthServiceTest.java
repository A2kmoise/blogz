package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.requests.ChangePasswordRequest;
import codewithmoise.org.blogbackend.DTO.requests.LoginRequest;
import codewithmoise.org.blogbackend.DTO.requests.UserRegistrationRequest;
import codewithmoise.org.blogbackend.DTO.responses.AuthenticationResponse;
import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.UserRepository;
import codewithmoise.org.blogbackend.util.JwtUtil;
import codewithmoise.org.blogbackend.util.OtpGenerator;
import codewithmoise.org.blogbackend.util.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private OtpGenerator otpGenerator;

    @InjectMocks
    private AuthService authService;

    @Test
    void createAccount_WhenEmailDoesNotExist_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("alice");
        request.setEmail("alice@test.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtUtil.generateToken("alice@test.com", 1L, UserRoles.AUTHOR.toString())).thenReturn("mockJwtToken");

        AuthenticationResponse response = authService.createAccount(request);

        assertTrue(response.isSuccess());
        assertEquals("Account created successfully", response.getMessage());
        assertEquals("mockJwtToken", response.getToken());
        assertEquals("alice", response.getUser().getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createAccount_WhenEmailExists_ReturnsFailureMessage() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existing@test.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        AuthenticationResponse response = authService.createAccount(request);

        assertFalse(response.isSuccess());
        assertEquals("Email already exists", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WhenUserDoesNotExist_ReturnsFailureMessage() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        AuthenticationResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
    }

    @Test
    void login_WhenCredentialsAreInvalid_ReturnsFailureMessage() {
        LoginRequest request = new LoginRequest();
        request.setEmail("bob@test.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setEmail("bob@test.com");
        user.setPassword("correctPasswordHash");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "correctPasswordHash")).thenReturn(false);

        AuthenticationResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid credentials", response.getMessage());
    }

    @Test
    void login_WhenCredentialsAreValid_ReturnsSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("bob@test.com");
        request.setPassword("correctPassword");

        User user = new User();
        user.setId(5L);
        user.setUsername("bob");
        user.setEmail("bob@test.com");
        user.setPassword("correctPasswordHash");
        user.setRole(UserRoles.AUTHOR);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "correctPasswordHash")).thenReturn(true);
        when(jwtUtil.generateToken("bob@test.com", 5L, UserRoles.AUTHOR.toString())).thenReturn("validToken");

        AuthenticationResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("validToken", response.getToken());
        assertEquals("bob", response.getUser().getName());
    }

    @Test
    void changePassword_WhenCurrentPasswordMatches_Success() {
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrent("oldPassword");
        request.setNext("newPassword");

        User user = new User();
        user.setId(userId);
        user.setPassword("oldPasswordHash");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldPasswordHash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newPasswordHash");

        assertDoesNotThrow(() -> authService.changePassword(userId, request));
        assertEquals("newPasswordHash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_WhenCurrentPasswordDoesNotMatch_ThrowsRuntimeException() {
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrent("wrongOldPassword");
        request.setNext("newPassword");

        User user = new User();
        user.setId(userId);
        user.setPassword("oldPasswordHash");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "oldPasswordHash")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.changePassword(userId, request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void forgotPassword_WhenUserExists_SendsOtpEmail() {
        String email = "forgot@test.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpGenerator.generateOtp()).thenReturn("123456");

        authService.forgotPassword(email);

        assertEquals("123456", user.getOtp());
        assertNotNull(user.getOtpExpiryDate());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void verifyOtp_WhenOtpIsValidAndNotExpired_ReturnsTrue() {
        String email = "forgot@test.com";
        User user = new User();
        user.setEmail(email);
        user.setOtp("123456");
        user.setOtpExpiryDate(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertTrue(authService.verifyOtp(email, "123456"));
    }

    @Test
    void verifyOtp_WhenOtpIsExpired_ThrowsRuntimeException() {
        String email = "forgot@test.com";
        User user = new User();
        user.setEmail(email);
        user.setOtp("123456");
        user.setOtpExpiryDate(LocalDateTime.now().minusMinutes(1)); // Expired

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.verifyOtp(email, "123456"));
    }

    @Test
    void verifyOtp_WhenOtpIsIncorrect_ThrowsRuntimeException() {
        String email = "forgot@test.com";
        User user = new User();
        user.setEmail(email);
        user.setOtp("123456");
        user.setOtpExpiryDate(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.verifyOtp(email, "999999"));
    }
}
