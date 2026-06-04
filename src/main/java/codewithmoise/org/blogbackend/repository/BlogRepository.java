package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.enums.BlogCategory;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByUser(User user);
    List<Blog> findByCategory(BlogCategory category);
    List<Blog> findByTitleContainingIgnoreCase(String title);
    Page<Blog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Optional<Blog> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT DISTINCT b FROM Blog b LEFT JOIN b.tags t " +
            "WHERE (:published IS NULL OR b.published = :published) " +
            "AND (:authorId IS NULL OR b.user.id = :authorId) " +
            "AND (:category IS NULL OR b.category = :category) " +
            "AND (:tag IS NULL OR LOWER(t.name) = LOWER(CAST(:tag AS string))) " +
            "AND (:search IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Blog> findBlogsWithFilters(
            @Param("published") Boolean published,
            @Param("authorId") Long authorId,
            @Param("category") BlogCategory category,
            @Param("tag") String tag,
            @Param("search") String search,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Blog b set b.published = true, b.scheduledAt = null WHERE b.published = false AND b.scheduledAt IS NOT NULL AND b.scheduledAt <= :now")
    int publishDueBlogs(@Param("now")LocalDateTime now);
}
