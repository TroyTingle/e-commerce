package productService.services;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import productservice.exceptions.CategoryNotFoundException;
import productservice.exceptions.DuplicateSkuException;
import productservice.exceptions.ProductNotFoundException;
import productservice.mappers.ProductMapper;
import productservice.models.Category;
import productservice.models.Product;
import productservice.models.dto.InventoryUpdateRequest;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductRequest;
import productservice.models.dto.ProductSearchRequest;
import productservice.repositories.CategoryRepository;
import productservice.repositories.ProductRepository;
import productservice.services.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  private static final UUID PRODUCT_ID = randomUUID();
  private static final String PRODUCT_SKU = "SKU-123";
  private static final String CATEGORY_NAME = "Electronics";

  @Mock private CategoryRepository categoryRepository;
  @Mock private ProductRepository productRepository;
  @Mock private ProductMapper productMapper;
  @InjectMocks private ProductService productService;

  @Test
  void whenGetProductByIdCalled_thenReturnMappedProduct() {
    Product product = buildProduct();
    ProductDto productDto = buildProductDto();

    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
    when(productMapper.toProductDto(product)).thenReturn(productDto);

    ProductDto response = productService.getProductById(PRODUCT_ID);

    assertThat(response).isEqualTo(productDto);
    verify(productRepository).findById(PRODUCT_ID);
    verify(productMapper).toProductDto(product);
  }

  @Test
  void whenGetProductByIdCalledAndProductDoesNotExist_thenThrowProductNotFoundException() {
    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.getProductById(PRODUCT_ID))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(PRODUCT_ID.toString());
  }

  @Test
  void whenGetAllProductsCalled_thenReturnMappedPage() {
    ProductSearchRequest searchRequest = Instancio.of(ProductSearchRequest.class).create();
    Pageable pageable = Pageable.ofSize(5);
    Product productOne = buildProduct();
    Product productTwo = buildProduct().toBuilder().id(randomUUID()).sku("SKU-456").build();
    ProductDto productDtoOne = buildProductDto();
    ProductDto productDtoTwo =
        buildProductDto().toBuilder().id(randomUUID()).sku("SKU-456").build();
    Page<Product> productPage = new PageImpl<>(List.of(productOne, productTwo));

    when(productRepository.findAll(
            argThat((Specification<Product> specification) -> specification != null), eq(pageable)))
        .thenReturn(productPage);
    when(productMapper.toProductDto(productOne)).thenReturn(productDtoOne);
    when(productMapper.toProductDto(productTwo)).thenReturn(productDtoTwo);

    Page<ProductDto> response = productService.getAllProducts(searchRequest, pageable);

    assertThat(response.getContent()).containsExactly(productDtoOne, productDtoTwo);
    verify(productRepository)
        .findAll(
            argThat((Specification<Product> specification) -> specification != null), eq(pageable));
    verify(productMapper).toProductDto(productOne);
    verify(productMapper).toProductDto(productTwo);
  }

  @Test
  void whenGetProductBySkuCalled_thenReturnMappedProduct() {
    Product product = buildProduct();
    ProductDto productDto = buildProductDto();

    when(productRepository.findBySku(PRODUCT_SKU)).thenReturn(Optional.of(product));
    when(productMapper.toProductDto(product)).thenReturn(productDto);

    ProductDto response = productService.getProductBySku(PRODUCT_SKU);

    assertThat(response).isEqualTo(productDto);
    verify(productRepository).findBySku(PRODUCT_SKU);
    verify(productMapper).toProductDto(product);
  }

  @Test
  void whenGetProductBySkuCalledAndProductDoesNotExist_thenThrowProductNotFoundException() {
    when(productRepository.findBySku(PRODUCT_SKU)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.getProductBySku(PRODUCT_SKU))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(PRODUCT_SKU);
  }

  @Test
  void whenCreateProductCalled_thenReturnCreatedProductDto() {
    ProductRequest request = buildProductRequest();
    Category category = buildCategory();
    Product mappedProduct = buildProduct();
    Product savedProduct = buildProduct();
    ProductDto productDto = buildProductDto();

    when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.of(category));
    when(productRepository.existsBySku(PRODUCT_SKU)).thenReturn(false);
    when(productMapper.toNewProduct(request, category)).thenReturn(mappedProduct);
    when(productRepository.save(mappedProduct)).thenReturn(savedProduct);
    when(productMapper.toProductDto(savedProduct)).thenReturn(productDto);

    ProductDto response = productService.createProduct(request);

    assertThat(response).isEqualTo(productDto);
    verify(categoryRepository).findByName(CATEGORY_NAME);
    verify(productRepository).existsBySku(PRODUCT_SKU);
    verify(productMapper).toNewProduct(request, category);
    verify(productRepository).save(mappedProduct);
    verify(productMapper).toProductDto(savedProduct);
  }

  @Test
  void whenCreateProductCalledAndCategoryMissing_thenThrowCategoryNotFoundException() {
    ProductRequest request = buildProductRequest();
    when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.createProduct(request))
        .isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining(CATEGORY_NAME);

    verify(productRepository, never()).existsBySku(PRODUCT_SKU);
    verify(productRepository, never()).save(isA(Product.class));
  }

  @Test
  void whenCreateProductCalledAndSkuExists_thenThrowDuplicateSkuException() {
    ProductRequest request = buildProductRequest();
    Category category = buildCategory();

    when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.of(category));
    when(productRepository.existsBySku(PRODUCT_SKU)).thenReturn(true);

    assertThatThrownBy(() -> productService.createProduct(request))
        .isInstanceOf(DuplicateSkuException.class)
        .hasMessageContaining(PRODUCT_SKU);

    verify(productRepository, never()).save(isA(Product.class));
  }

  @Test
  void whenUpdateProductCalled_thenReturnUpdatedProductDto() {
    ProductRequest request = buildProductRequest();
    Product existingProduct = buildProduct();
    Category category = buildCategory();
    Product updatedProduct = buildProduct().toBuilder().name("Updated Product").build();
    ProductDto updatedProductDto = buildProductDto().toBuilder().name("Updated Product").build();

    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
    when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.of(category));
    when(productMapper.toUpdatedProduct(existingProduct, request, category))
        .thenReturn(updatedProduct);
    when(productRepository.save(updatedProduct)).thenReturn(updatedProduct);
    when(productMapper.toProductDto(updatedProduct)).thenReturn(updatedProductDto);

    ProductDto response = productService.updateProduct(PRODUCT_ID, request);

    assertThat(response).isEqualTo(updatedProductDto);
    verify(productRepository).findById(PRODUCT_ID);
    verify(categoryRepository).findByName(CATEGORY_NAME);
    verify(productMapper).toUpdatedProduct(existingProduct, request, category);
    verify(productRepository).save(updatedProduct);
    verify(productMapper).toProductDto(updatedProduct);
  }

  @Test
  void whenUpdateProductCalledAndProductMissing_thenThrowProductNotFoundException() {
    ProductRequest request = buildProductRequest();
    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(PRODUCT_ID.toString());
  }

  @Test
  void whenUpdateProductCalledAndCategoryMissing_thenThrowCategoryNotFoundException() {
    ProductRequest request = buildProductRequest();
    Product existingProduct = buildProduct();

    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));
    when(categoryRepository.findByName(CATEGORY_NAME)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request))
        .isInstanceOf(CategoryNotFoundException.class)
        .hasMessageContaining(CATEGORY_NAME);

    verify(productRepository, never()).save(isA(Product.class));
  }

  @Test
  void whenDeactivateProductCalled_thenSaveInactiveProduct() {
    Product existingProduct = buildProduct().toBuilder().active(true).build();
    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));

    productService.deactivateProduct(PRODUCT_ID);

    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(productCaptor.capture());
    assertThat(productCaptor.getValue().getActive()).isFalse();
  }

  @Test
  void whenUpdateInventoryCalled_thenSaveProductWithUpdatedQuantity() {
    Product existingProduct = buildProduct().toBuilder().inventoryQuantity(10).build();
    InventoryUpdateRequest request = InventoryUpdateRequest.builder().quantity(99).build();
    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(existingProduct));

    productService.updateInventory(PRODUCT_ID, request);

    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(productCaptor.capture());
    assertThat(productCaptor.getValue().getInventoryQuantity()).isEqualTo(99);
  }

  @Test
  void whenDeactivateProductCalledAndProductMissing_thenThrowProductNotFoundException() {
    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.deactivateProduct(PRODUCT_ID))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(PRODUCT_ID.toString());
  }

  @Test
  void whenUpdateInventoryCalledAndProductMissing_thenThrowProductNotFoundException() {
    InventoryUpdateRequest request = InventoryUpdateRequest.builder().quantity(20).build();
    when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.updateInventory(PRODUCT_ID, request))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(PRODUCT_ID.toString());
  }

  private Product buildProduct() {
    Category category = buildCategory();
    return Instancio.of(Product.class)
        .set(field(Product::getId), PRODUCT_ID)
        .set(field(Product::getSku), PRODUCT_SKU)
        .set(field(Product::getCategory), category)
        .set(field(Product::getActive), true)
        .create();
  }

  private ProductRequest buildProductRequest() {
    return Instancio.of(ProductRequest.class)
        .set(field(ProductRequest::getSku), PRODUCT_SKU)
        .set(field(ProductRequest::getCategoryName), CATEGORY_NAME)
        .create();
  }

  private ProductDto buildProductDto() {
    return Instancio.of(ProductDto.class)
        .set(field(ProductDto::getId), PRODUCT_ID)
        .set(field(ProductDto::getSku), PRODUCT_SKU)
        .set(field(ProductDto::getCategory), CATEGORY_NAME)
        .create();
  }

  private Category buildCategory() {
    return Instancio.of(Category.class).set(field(Category::getName), CATEGORY_NAME).create();
  }
}
