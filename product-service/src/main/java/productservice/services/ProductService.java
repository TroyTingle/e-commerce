package productservice.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import productservice.exceptions.ProductNotFoundException;
import productservice.mappers.ProductMapper;
import productservice.models.Product;
import productservice.models.dto.ProductDto;
import productservice.repositories.ProductRepository;

import java.util.List;
import java.util.UUID;

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

  public List<ProductDto> getAllProducts(){
    return productRepository.findAll()
        .stream()
        .map(productMapper::toProductDto)
        .toList();
  }

}
