package codewithmoise.org.blogbackend.models;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "blog_tags",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Column(name = "tags")
    private List<Tag> tags = new ArrayList<>();

    public void setTags(List<String> tags) {
    }
    public List<String >getTags(){
        return Collections.singletonList(tags.toString());
    }
}
