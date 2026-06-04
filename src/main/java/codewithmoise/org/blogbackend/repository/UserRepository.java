package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRoles role);
    long countByRole(UserRoles role);
    long countBySuspended(boolean suspended);

    @Query("SELECT u FROM User u WHERE u.role = :role " +
            "AND (:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> findByRoleAndSearch(
            @Param("role") UserRoles role,
            @Param("search") String search
    );
}
