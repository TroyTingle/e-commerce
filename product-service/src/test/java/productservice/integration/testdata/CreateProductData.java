package productservice.integration.testdata;

import static org.instancio.Select.field;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.springframework.boot.test.context.TestComponent;
import productservice.models.Category;
import productservice.repositories.CategoryRepository;

@TestComponent
@RequiredArgsConstructor
public class CreateProductData {

  private final CategoryRepository categoryRepository;

  public Category createCategoryWithName(String name) {
    Category category =
        Instancio.of(Category.class)
            .ignore(field(Category::getId))
            .set(field(Category::getName), name)
            .create();

    return categoryRepository.save(category);
  }
}
