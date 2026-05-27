package codewithmoise.org.blogbackend.models;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private BlogCategory category;

    @Column(name = "tags")
    private String Tags;
}
