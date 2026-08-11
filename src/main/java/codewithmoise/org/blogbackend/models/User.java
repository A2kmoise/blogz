package codewithmoise.org.blogbackend.models;

import codewithmoise.org.blogbackend.enums.UserRoles;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="username")
    private String username;

    @Column(unique = true)
    private String email;

    private String otp;

    private LocalDateTime otpExpiryDate;

    @Column(name = "password")
    private String password;

    // Password Reset Fields
    @Column(unique = true)
    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiryDate;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean passwordResetRequested;

    private LocalDateTime lastPasswordResetRequestTime;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRoles role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Blog> blogs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean suspended = false;

    public User() {
    }
}
