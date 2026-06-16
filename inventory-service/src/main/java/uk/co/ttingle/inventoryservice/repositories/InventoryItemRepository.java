package uk.co.ttingle.inventoryservice.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.ttingle.inventoryservice.models.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

  Optional<InventoryItem> findByProductId(UUID productId);
}
