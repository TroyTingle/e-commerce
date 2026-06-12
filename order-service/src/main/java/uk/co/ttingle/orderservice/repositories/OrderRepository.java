package uk.co.ttingle.orderservice.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.ttingle.orderservice.models.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

  List<Order> findAllByUserId(UUID userId);
}
