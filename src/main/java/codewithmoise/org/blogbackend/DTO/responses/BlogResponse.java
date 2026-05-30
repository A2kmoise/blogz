package codewithmoise.org.blogbackend.DTO.responses;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogResponse {

    private Long id;

    private String title;

    private String content;

    private BlogCategory category;

    private List<String> tags;

    private Long authorId;

    private LocalDateTime createdAt;
}