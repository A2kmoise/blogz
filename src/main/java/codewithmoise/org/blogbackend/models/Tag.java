package codewithmoise.org.blogbackend.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
