package codewithmoise.org.blogbackend.models;

import codewithmoise.org.blogbackend.enums.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRoles role;

    @OneToMany(mappedBy = "user")
    private List<Blog> blogs = new ArrayList<>();

    public User() {
    }

}
