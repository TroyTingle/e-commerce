package uk.co.ttingle.productservice.mappers;

import org.springframework.stereotype.Component;
import uk.co.ttingle.productservice.models.Category;
import uk.co.ttingle.productservice.generated.rest.v1.dto.CategoryDto;

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
