package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.models.Tag;
import codewithmoise.org.blogbackend.repository.TagRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> findOrCreateTags(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();
        
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        
        return tags;
    }

    @Cacheable(value = "tags")
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}