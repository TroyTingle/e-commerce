package uk.co.ttingle.inventoryservice.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.ttingle.inventoryservice.models.StockAdjustment;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, UUID> {

  List<StockAdjustment> findAllByProductIdOrderByCreatedAtDesc(UUID productId);
}
