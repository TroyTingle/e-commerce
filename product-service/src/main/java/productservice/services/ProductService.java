package productservice.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import productservice.mappers.ProductMapper;
import productservice.models.dto.ProductDto;
import productservice.repositories.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  public ProductDto getProductById(UUID id){
    return productMapper.toProductDto(productRepository.findById(id).orElseThrow());
  }

}
