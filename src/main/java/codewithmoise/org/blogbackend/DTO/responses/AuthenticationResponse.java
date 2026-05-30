package codewithmoise.org.blogbackend.DTO.responses;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String message;
    private UserResponse user;
    private String token;
    private boolean success;

    public AuthenticationResponse(String message, UserResponse user, String token, boolean success) {
        this.message = message;
        this.user = user;
        this.token = token;
        this.success = success;
    }

    public AuthenticationResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}