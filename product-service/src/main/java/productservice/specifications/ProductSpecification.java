package productservice.specifications;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import productservice.models.Product;

import java.math.BigDecimal;

public class ProductSpecification {

  public static Specification<Product> hasCategory(String categoryName) {
    return (root, _, cb) -> {
      if (categoryName == null || categoryName.isBlank()) {
        return null;
      }

      return cb.equal(
          root.join("category", JoinType.LEFT).get("name"),
          categoryName
      );
    };
  }

  public static Specification<Product> minPrice(BigDecimal minPrice) {
    return (root, _, cb) -> {
      if (minPrice == null) {
        return null;
      }
      return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    };
  }

  public static Specification<Product> maxPrice(BigDecimal maxPrice) {
    return (root, _, cb) -> {
      if (maxPrice == null) {
        return null;
      }
      return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    };
  }

  public static Specification<Product> nameContains(String name) {
    return (root, _, cb) -> {
      if (name == null || name.isBlank()) {
        return null;
      }
      return cb.like(
          cb.lower(root.get("name")),
          "%" + name.toLowerCase() + "%"
      );
    };
  }

  public static Specification<Product> isActive() {
    return (root, _, cb) ->
        cb.isTrue(root.get("active"));
  }
}