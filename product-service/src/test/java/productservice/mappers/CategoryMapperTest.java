package productservice.mappers;

import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import productservice.models.Category;
import productservice.models.dto.CategoryDto;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CategoryMapperTest {

  private static final CategoryMapper categoryMapper = new CategoryMapper();

  @Test
  void whenToProductDtoCalled_thenFieldsAreMapped() {
    Category category = Instancio.of(Category.class).create();

    CategoryDto result = categoryMapper.toCategoryDto(category);

    assertThat(result.getId()).isEqualTo(category.getId());
    assertThat(result.getName()).isEqualTo(category.getName());
    assertThat(result.getDescription()).isEqualTo(category.getDescription());
  }
}
