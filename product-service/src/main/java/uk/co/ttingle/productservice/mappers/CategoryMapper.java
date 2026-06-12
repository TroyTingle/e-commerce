package uk.co.ttingle.productservice.mappers;

import org.springframework.stereotype.Component;
import uk.co.ttingle.productservice.generated.rest.v1.dto.CategoryDto;
import uk.co.ttingle.productservice.models.Category;

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
