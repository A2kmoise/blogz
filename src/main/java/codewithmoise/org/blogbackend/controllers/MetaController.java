package codewithmoise.org.blogbackend.controllers;

import codewithmoise.org.blogbackend.DTO.responses.BlogResponse.CategoryResponse;
import codewithmoise.org.blogbackend.enums.BlogCategory;
import codewithmoise.org.blogbackend.models.Tag;
import codewithmoise.org.blogbackend.services.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MetaController {

    private final TagService tagService;

    public MetaController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> categories = Arrays.stream(BlogCategory.values())
                .map(cat -> {
                    String name = cat.name();
                    return new CategoryResponse(name, name, name.toLowerCase());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTags() {
        List<String> tags = tagService.getAllTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }
}
