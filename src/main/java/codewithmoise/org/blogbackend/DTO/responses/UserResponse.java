package codewithmoise.org.blogbackend.DTO.responses;

import codewithmoise.org.blogbackend.enums.UserRoles;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private UserRoles role;
    private boolean suspended;
    private LocalDateTime createdAt;
}