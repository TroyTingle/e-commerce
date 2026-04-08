package productservice.services;

import static productservice.specifications.ProductSpecification.hasCategory;
import static productservice.specifications.ProductSpecification.isActive;
import static productservice.specifications.ProductSpecification.maxPrice;
import static productservice.specifications.ProductSpecification.minPrice;
import static productservice.specifications.ProductSpecification.nameContains;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import productservice.exceptions.CategoryNotFoundException;
import productservice.exceptions.DuplicateSkuException;
import productservice.exceptions.ProductNotFoundException;
import productservice.mappers.ProductMapper;
import productservice.models.Category;
import productservice.models.Product;
import productservice.models.dto.InventoryUpdateRequest;
import productservice.models.dto.ProductRequest;
import productservice.models.dto.ProductSearchRequest;
import productservice.repositories.CategoryRepository;
import productservice.repositories.ProductRepository;
import uk.co.ttingle.commonlib.dto.ProductDto;

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
  public Page<ProductDto> getAllProducts(ProductSearchRequest searchRequest, Pageable pageable) {

    Specification<Product> spec =
        Specification.allOf(
            hasCategory(searchRequest.getCategory()),
            minPrice(searchRequest.getMinPrice()),
            maxPrice(searchRequest.getMaxPrice()),
            nameContains(searchRequest.getSearch()),
            isActive(searchRequest.getActive()));

    return productRepository.findAll(spec, pageable).map(productMapper::toProductDto);
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
}
