package codewithmoise.org.blogbackend.DTO.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegistrationRequest {

    @Pattern(regexp = "^[a-zA-Z]+$", message = "no numeric characters or spaces are allowed in username lol")
    @NotBlank(message = "username is required")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Length(min = 6, message = "Password should be at least 6 characters")
    private String password;
}