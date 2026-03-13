package productservice.mappers;

import org.springframework.stereotype.Component;
import productservice.models.Category;
import productservice.models.dto.CategoryDto;

@Component
public class CategoryMapper {

  public CategoryDto toCategoryDto(Category category) {
    return CategoryDto.builder()
        .id(category.getId())
        .name(category.getName())
        .description(category.getDescription())
        .build();
  }
}
