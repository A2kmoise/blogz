package codewithmoise.org.blogbackend.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tags")
@Data
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}
