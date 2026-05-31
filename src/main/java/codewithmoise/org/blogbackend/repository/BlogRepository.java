package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByUser(User user);
    List<Blog> findByCategory(BlogCategory category);
    List<Blog> findByTitleContainingIgnoreCase(String title);
   Page<Blog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
