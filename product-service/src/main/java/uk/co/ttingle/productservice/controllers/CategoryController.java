package uk.co.ttingle.productservice.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.productservice.generated.rest.v1.CategoriesApiV1;
import uk.co.ttingle.productservice.generated.rest.v1.dto.CategoryDto;
import uk.co.ttingle.productservice.services.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoriesApiV1 {

  private final CategoryService categoryService;

  @Override
  public ResponseEntity<List<CategoryDto>> getAllCategories() {
    return ResponseEntity.ok(categoryService.getAllCategories());
  }

  @Override
  public ResponseEntity<CategoryDto> getCategoryByName(@PathVariable String name) {
    return ResponseEntity.ok(categoryService.getCategoryByName(name));
  }
}
