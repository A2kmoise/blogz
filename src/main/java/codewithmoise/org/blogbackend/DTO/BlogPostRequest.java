package codewithmoise.org.blogbackend.DTO;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class BlogPostRequest {

    @NotEmpty
    String title;

    @NotEmpty
    String content;

    @NotEmpty
    @Enumerated(EnumType.STRING)
    BlogCategory category;

    String tags;
}
