package productservice.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import productservice.models.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Optional<Category> findByName(String categoryName);
}
