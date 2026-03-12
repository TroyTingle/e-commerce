package productservice.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import productservice.exceptions.CategoryNotFoundException;
import productservice.mappers.CategoryMapper;
import productservice.models.dto.CategoryDto;
import productservice.repositories.CategoryRepository;

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
