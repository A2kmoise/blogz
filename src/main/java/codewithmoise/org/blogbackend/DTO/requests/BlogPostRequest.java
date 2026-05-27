package codewithmoise.org.blogbackend.DTO.requests;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class BlogPostRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "content is required")
    private String content;

    @NotNull(message = "Invalid category or empty category")
    private BlogCategory category;

    private List<String> tags;

    private Long userId;
}
