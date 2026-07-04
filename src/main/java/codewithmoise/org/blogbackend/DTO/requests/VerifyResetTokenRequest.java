package codewithmoise.org.blogbackend.DTO.requests;

import lombok.Data;

@Data
public class VerifyResetTokenRequest {
    private String token;
}
