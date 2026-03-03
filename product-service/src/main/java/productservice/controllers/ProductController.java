package productservice.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import productservice.models.dto.ProductDto;
import productservice.services.ProductService;

import java.util.UUID;

@RestController("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;


  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(@RequestParam UUID id){
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @GetMapping
  public ResponseEntity<?> getAllProducts(){
    return ResponseEntity.ok(productService.getAllProducts());
  }
}
