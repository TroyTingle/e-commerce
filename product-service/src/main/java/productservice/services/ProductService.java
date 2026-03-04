package productservice.services;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import productservice.exceptions.ProductNotFoundException;
import productservice.mappers.ProductMapper;
import productservice.models.Product;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductSearchRequest;
import productservice.repositories.ProductRepository;

import java.util.UUID;

import static productservice.specifications.ProductSpecification.hasCategory;
import static productservice.specifications.ProductSpecification.isActive;
import static productservice.specifications.ProductSpecification.maxPrice;
import static productservice.specifications.ProductSpecification.minPrice;
import static productservice.specifications.ProductSpecification.nameContains;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  public ProductDto getProductById(UUID id){
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(String.format("No product found with id %s", id)));
    return productMapper.toProductDto(product);
  }

  public Page<ProductDto> getAllProducts(ProductSearchRequest searchRequest,
                                         Pageable pageable){

    Specification<Product> spec = Specification
        .where(hasCategory(searchRequest.getCategory()))
        .and(minPrice(searchRequest.getMinPrice()))
        .and(maxPrice(searchRequest.getMaxPrice()))
        .and(nameContains(searchRequest.getSearch()))
        .and(searchRequest.getActive() != null && searchRequest.getActive() ? isActive() : null);

    return productRepository.findAll(spec, pageable)
        .map(productMapper::toProductDto);
  }

  public ProductDto getProductBySku(String sku){
    Product product = productRepository.findBySku(sku)
        .orElseThrow(() -> new ProductNotFoundException(String.format("No product found with sku %s", sku)));
    return productMapper.toProductDto(product);
  }

}
