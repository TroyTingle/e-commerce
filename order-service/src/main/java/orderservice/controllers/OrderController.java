package orderservice.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import orderservice.models.dto.OrderUpdateRequest;
import orderservice.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(
      @RequestBody @Valid OrderRequestDto orderRequestDto) {
    return ResponseEntity.status(CREATED).body(orderService.createOrder(orderRequestDto));
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderId") UUID orderId) {
    return ResponseEntity.ok(orderService.getOrderById(orderId));
  }

  @GetMapping
  public ResponseEntity<List<OrderResponse>> getAllOrdersForUser(UUID userId) {
    return ResponseEntity.ok(orderService.getOrdersForUser(userId));
  }

  @PatchMapping("/{orderId}")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable("userId") UUID orderId, @RequestBody @Valid OrderUpdateRequest orderUpdate) {
    orderService.updateOrderStatus(orderId, orderUpdate);
    return ResponseEntity.status(NO_CONTENT).build();
  }
}
