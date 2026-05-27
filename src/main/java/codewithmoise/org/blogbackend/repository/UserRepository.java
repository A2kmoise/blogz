package codewithmoise.org.blogbackend.repository;

import codewithmoise.org.blogbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
