package productservice.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import productservice.models.dto.InventoryUpdateRequest;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductRequest;
import productservice.services.ProductService;

// TODO: Add @PreAuthorize("hasRole('ADMIN')")
@RestController("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

  private final ProductService productService;

  @PostMapping
  public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid ProductRequest request) {
    return ResponseEntity.status(CREATED).body(productService.createProduct(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable UUID id, @RequestBody @Valid ProductRequest request) {
    return ResponseEntity.ok(productService.updateProduct(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
    productService.deactivateProduct(id);
    return ResponseEntity.status(NO_CONTENT).build();
  }

  @PatchMapping("/{id}/inventory")
  public ResponseEntity<Void> updateInventory(
      @PathVariable UUID id, @RequestBody @Valid InventoryUpdateRequest request) {
    productService.updateInventory(id, request);
    return ResponseEntity.status(NO_CONTENT).build();
  }
}
