package codewithmoise.org.blogbackend.DTO.requests;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import lombok.Data;

import java.util.List;

@Data
public class BlogUpdateRequest {

    private String title;

    private String content;

    private String excerpt;

    private String coverImage;

    private String categoryId;

    private List<String> tags;

    private Boolean published;
}
