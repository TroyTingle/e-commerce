package uk.co.ttingle.productservice.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.co.ttingle.productservice.exceptions.CategoryNotFoundException;
import uk.co.ttingle.productservice.generated.rest.v1.dto.CategoryDto;
import uk.co.ttingle.productservice.mappers.CategoryMapper;
import uk.co.ttingle.productservice.repositories.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  public List<CategoryDto> getAllCategories() {
    return categoryRepository.findAll().stream().map(categoryMapper::toCategoryDto).toList();
  }

  public CategoryDto getCategoryByName(String name) {
    return categoryMapper.toCategoryDto(
        categoryRepository.findByName(name).orElseThrow(() -> new CategoryNotFoundException(name)));
  }
}
