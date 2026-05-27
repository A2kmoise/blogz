package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.models.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
