package codewithmoise.org.blogbackend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AuthenticationRequest {

    @NotNull
    String email;

    @NotNull
    @Length(min = 6)
    String password;
}
