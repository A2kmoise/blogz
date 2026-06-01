package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRoles role);
    long countByRole(UserRoles role);
    long countBySuspended(boolean suspended);
}
