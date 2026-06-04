package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    @Modifying
    @Query("DELETE FROM Tag t WHERE SIZE(t.blogs) = 0 ")
    void deleteOrphanedTags();

}
