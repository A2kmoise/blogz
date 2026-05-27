package codewithmoise.org.blogbackend.DTO.requests;


import lombok.Data;

import java.util.List;

@Data
public class BlogUpdateRequest {

    private  String content;


    private List<String> tags;
}
