package uk.co.ttingle.productservice.controllers;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.productservice.generated.rest.v1.AdminProductsApiV1;
import uk.co.ttingle.productservice.generated.rest.v1.dto.InventoryUpdateRequest;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductDto;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductRequest;
import uk.co.ttingle.productservice.services.ProductService;

// TODO: Add @PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController implements AdminProductsApiV1 {

  private final ProductService productService;

  @Override
  public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid ProductRequest request) {
    return ResponseEntity.status(CREATED).body(productService.createProduct(request));
  }

  @Override
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable UUID id, @RequestBody @Valid ProductRequest request) {
    return ResponseEntity.ok(productService.updateProduct(id, request));
  }

  @Override
  public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
    productService.deactivateProduct(id);
    return ResponseEntity.status(NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<Void> updateInventory(
      @PathVariable UUID id, @RequestBody @Valid InventoryUpdateRequest request) {
    productService.updateInventory(id, request);
    return ResponseEntity.status(NO_CONTENT).build();
  }
}
