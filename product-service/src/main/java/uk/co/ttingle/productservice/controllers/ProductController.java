package uk.co.ttingle.productservice.controllers;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.productservice.generated.rest.v1.ProductsApiV1;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductDto;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductPage;
import uk.co.ttingle.productservice.services.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductsApiV1 {

  private final ProductService productService;

  @Override
  public ResponseEntity<ProductPage> getAllProducts(
      String category,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String search,
      Boolean active,
      Pageable pageable) {
    return ResponseEntity.ok(
        productService.getAllProducts(category, minPrice, maxPrice, search, active, pageable));
  }

  @Override
  public ResponseEntity<ProductDto> getProductById(@PathVariable UUID id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @Override
  public ResponseEntity<ProductDto> getProductBySku(@PathVariable String sku) {
    return ResponseEntity.ok(productService.getProductBySku(sku));
  }
}
