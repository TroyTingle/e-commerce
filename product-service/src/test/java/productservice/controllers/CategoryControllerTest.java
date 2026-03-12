package productservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import productservice.models.dto.CategoryDto;
import productservice.services.CategoryService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

  private static final String CATEGORY_NAME = "Test Category Name";

  @Mock private CategoryService categoryService;

  @InjectMocks private CategoryController categoryController;

  @Test
  void whenGetAllCategoriesCalled_thenReturnListOfCategoryDto() {
    List<CategoryDto> categoryDtoList = Instancio.ofList(CategoryDto.class).size(5).create();

    when(categoryService.getAllCategories()).thenReturn(categoryDtoList);

    ResponseEntity<List<CategoryDto>> response = categoryController.getAllCategories();

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(categoryDtoList);
  }

  @Test
  void whenGetProductByIdCalled_thenReturnProductDto() {
    CategoryDto categoryDto = Instancio.of(CategoryDto.class).create();

    when(categoryService.getCategoryByName(CATEGORY_NAME)).thenReturn(categoryDto);

    ResponseEntity<CategoryDto> response = categoryController.getCategoryByName(CATEGORY_NAME);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isInstanceOf(CategoryDto.class).isEqualTo(categoryDto);
  }
}
