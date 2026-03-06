package uk.co.ttingle.userservice.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.ttingle.userservice.models.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);
}
