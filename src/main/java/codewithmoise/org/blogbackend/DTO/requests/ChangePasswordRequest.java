package codewithmoise.org.blogbackend.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String current;

    @NotBlank(message = "New password is required")
    private String next;
}
