package codewithmoise.org.blogbackend.DTO.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AuthenticationRequest {

    @Email(message = "invalid email")
    @NotBlank(message = "message is required")
    private String email;

    @NotBlank(message = "password is required")
    @Length(min = 6,
    message = "password should be at least characters")
    private  String password;
}
