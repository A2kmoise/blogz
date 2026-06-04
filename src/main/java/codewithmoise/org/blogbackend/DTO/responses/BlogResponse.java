package codewithmoise.org.blogbackend.DTO.responses;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BlogResponse {

    private Long id;
    private String slug;
    private String title;
    private String excerpt;
    private String content;
    private String coverImage;
    private CategoryResponse category;
    private List<String> tags;
    private AuthorResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime scheduledAt;
    private int views;
    private boolean published;

    @Data
    public static class CategoryResponse {
        private String id;
        private String name;
        private String slug;

        public CategoryResponse(String id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }
    }

    @Data
    public static class AuthorResponse {
        private Long id;
        private String name;
        private String email;

        public AuthorResponse(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }
}