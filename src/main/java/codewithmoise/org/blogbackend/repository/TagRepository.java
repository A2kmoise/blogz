package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
