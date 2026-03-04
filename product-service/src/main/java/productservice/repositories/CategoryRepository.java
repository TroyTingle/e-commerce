package productservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import productservice.models.Category;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
