package uk.co.ttingle.productservice.services;

import static uk.co.ttingle.productservice.specifications.ProductSpecification.hasCategory;
import static uk.co.ttingle.productservice.specifications.ProductSpecification.isActive;
import static uk.co.ttingle.productservice.specifications.ProductSpecification.maxPrice;
import static uk.co.ttingle.productservice.specifications.ProductSpecification.minPrice;
import static uk.co.ttingle.productservice.specifications.ProductSpecification.nameContains;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ttingle.productservice.exceptions.CategoryNotFoundException;
import uk.co.ttingle.productservice.exceptions.DuplicateSkuException;
import uk.co.ttingle.productservice.exceptions.ProductNotFoundException;
import uk.co.ttingle.productservice.generated.rest.v1.dto.InventoryUpdateRequest;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductDto;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductPage;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductRequest;
import uk.co.ttingle.productservice.mappers.ProductMapper;
import uk.co.ttingle.productservice.models.Category;
import uk.co.ttingle.productservice.models.Product;
import uk.co.ttingle.productservice.repositories.CategoryRepository;
import uk.co.ttingle.productservice.repositories.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Transactional(readOnly = true)
  public ProductDto getProductById(UUID id) {
    Product product = getProductByIdOrThrow(id);
    return productMapper.toProductDto(product);
  }

  @Transactional(readOnly = true)
  public ProductPage getAllProducts(
      String category,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String search,
      Boolean active,
      Pageable pageable) {

    Specification<Product> spec =
        Specification.allOf(
            hasCategory(category),
            minPrice(minPrice),
            maxPrice(maxPrice),
            nameContains(search),
            isActive(active));

    Page<ProductDto> products =
        productRepository.findAll(spec, pageable).map(productMapper::toProductDto);
    return mapPageProductDtoToProductPage(products);
  }

  public ProductDto getProductBySku(String sku) {
    Product product =
        productRepository
            .findBySku(sku)
            .orElseThrow(
                () ->
                    new ProductNotFoundException(
                        String.format("No product found with sku %s", sku)));
    return productMapper.toProductDto(product);
  }

  @Transactional
  public ProductDto createProduct(ProductRequest request) {
    Category category =
        categoryRepository
            .findByName(request.getCategoryName())
            .orElseThrow(
                () ->
                    new CategoryNotFoundException(
                        String.format(
                            "No category found with name %s", request.getCategoryName())));

    if (productRepository.existsBySku(request.getSku())) {
      throw new DuplicateSkuException(
          String.format("Product with sku %s already exists", request.getSku()));
    }

    Product newProduct = productRepository.save(productMapper.toNewProduct(request, category));
    return productMapper.toProductDto(newProduct);
  }

  @Transactional
  public ProductDto updateProduct(UUID id, ProductRequest request) {
    Product existingProduct = getProductByIdOrThrow(id);

    Category category =
        categoryRepository
            .findByName(request.getCategoryName())
            .orElseThrow(
                () ->
                    new CategoryNotFoundException(
                        String.format(
                            "No category found with name %s", request.getCategoryName())));

    Product updatedProduct =
        productRepository.save(productMapper.toUpdatedProduct(existingProduct, request, category));

    return productMapper.toProductDto(updatedProduct);
  }

  @Transactional
  public void deactivateProduct(UUID id) {
    Product existingProduct = getProductByIdOrThrow(id);

    productRepository.save(existingProduct.toBuilder().active(false).build());
  }

  @Transactional
  public void updateInventory(UUID id, InventoryUpdateRequest inventoryUpdateRequest) {
    Product existingProduct = getProductByIdOrThrow(id);
    productRepository.save(
        existingProduct.toBuilder()
            .inventoryQuantity(inventoryUpdateRequest.getQuantity())
            .build());
  }

  private Product getProductByIdOrThrow(UUID id) {
    return productRepository
        .findById(id)
        .orElseThrow(
            () -> new ProductNotFoundException(String.format("No product found with id %s", id)));
  }

  private static ProductPage mapPageProductDtoToProductPage(Page<ProductDto> productDtoPage) {
    return ProductPage.builder()
        .content(productDtoPage.getContent())
        .totalPages(productDtoPage.getTotalPages())
        .totalElements(productDtoPage.getTotalElements())
        .build();
  }
}
