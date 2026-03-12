package productservice.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import productservice.models.dto.CategoryDto;
import productservice.services.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<List<CategoryDto>> getAllCategories() {
    return ResponseEntity.ok(categoryService.getAllCategories());
  }

  @GetMapping("/{name}")
  public ResponseEntity<CategoryDto> getCategoryByName(@RequestParam String name) {
    return ResponseEntity.ok(categoryService.getCategoryByName(name));
  }
}
