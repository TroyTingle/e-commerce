package orderservice.repositories;

import java.util.List;
import java.util.UUID;
import orderservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

  List<Order> findAllByUserId(UUID userId);
}
