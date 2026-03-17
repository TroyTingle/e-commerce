package productservice.controllers;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.commonlib.dto.ProductDto;
import productservice.models.dto.ProductSearchRequest;
import productservice.services.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<Page<ProductDto>> getAllProducts(
      @ModelAttribute @Valid ProductSearchRequest searchRequest, Pageable pageable) {
    return ResponseEntity.ok(productService.getAllProducts(searchRequest, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable UUID id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @GetMapping("/sku/{sku}")
  public ResponseEntity<ProductDto> getProductBySku(@PathVariable String sku) {
    return ResponseEntity.ok(productService.getProductBySku(sku));
  }
}
