package productService.specifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import productservice.models.Product;
import productservice.specifications.ProductSpecification;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationTest {

  @Mock
  private Root<Product> root;
  @Mock
  private CriteriaQuery<?> query;
  @Mock
  private CriteriaBuilder criteriaBuilder;
  @Mock
  private Predicate predicate;
  @Mock
  private Join<Object, Object> categoryJoin;
  @Mock
  private
  Path<Object> categoryNamePath;
  @Mock
  private Path<BigDecimal> pricePath;
  @Mock
  private Path<String> namePath;
  @Mock
  private Expression<String> lowerNameExpression;
  @Mock
  private Path<Boolean> activePath;

  @Test
  void hasCategoryWhenValuePresent_thenBuildsCategoryPredicate() {
    Specification<Product> specification = ProductSpecification.hasCategory("Electronics");

    when(root.join("category", JoinType.LEFT)).thenReturn(categoryJoin);
    when(categoryJoin.get("name")).thenReturn(categoryNamePath);
    when(criteriaBuilder.equal(categoryNamePath, "Electronics")).thenReturn(predicate);

    Predicate result = specification.toPredicate(root, query, criteriaBuilder);

    assertThat(result).isEqualTo(predicate);
    verify(root).join("category", JoinType.LEFT);
    verify(categoryJoin).get("name");
    verify(criteriaBuilder).equal(categoryNamePath, "Electronics");
  }

  @Test
  void hasCategoryWhenNullOrBlank_thenReturnsNullPredicate() {
    Specification<Product> nullSpecification = ProductSpecification.hasCategory(null);
    Specification<Product> blankSpecification = ProductSpecification.hasCategory("   ");

    assertThat(nullSpecification.toPredicate(root, query, criteriaBuilder)).isNull();
    assertThat(blankSpecification.toPredicate(root, query, criteriaBuilder)).isNull();
    verifyNoInteractions(root, criteriaBuilder);
  }

  @Test
  void minPriceWhenValuePresent_thenBuildsMinPricePredicate() {
    BigDecimal minPrice = BigDecimal.valueOf(10);
    Specification<Product> specification = ProductSpecification.minPrice(minPrice);

    when(root.<BigDecimal>get("price")).thenReturn(pricePath);
    when(criteriaBuilder.greaterThanOrEqualTo(pricePath, minPrice)).thenReturn(predicate);

    Predicate result = specification.toPredicate(root, query, criteriaBuilder);

    assertThat(result).isEqualTo(predicate);
    verify(root).get("price");
    verify(criteriaBuilder).greaterThanOrEqualTo(pricePath, minPrice);
  }

  @Test
  void minPriceWhenNull_thenReturnsNullPredicate() {
    Specification<Product> specification = ProductSpecification.minPrice(null);

    assertThat(specification.toPredicate(root, query, criteriaBuilder)).isNull();
    verifyNoInteractions(root, criteriaBuilder);
  }

  @Test
  void maxPriceWhenValuePresent_thenBuildsMaxPricePredicate() {
    BigDecimal maxPrice = BigDecimal.valueOf(100);
    Specification<Product> specification = ProductSpecification.maxPrice(maxPrice);

    when(root.<BigDecimal>get("price")).thenReturn(pricePath);
    when(criteriaBuilder.lessThanOrEqualTo(pricePath, maxPrice)).thenReturn(predicate);

    Predicate result = specification.toPredicate(root, query, criteriaBuilder);

    assertThat(result).isEqualTo(predicate);
    verify(root).get("price");
    verify(criteriaBuilder).lessThanOrEqualTo(pricePath, maxPrice);
  }

  @Test
  void maxPriceWhenNull_thenReturnsNullPredicate() {
    Specification<Product> specification = ProductSpecification.maxPrice(null);

    assertThat(specification.toPredicate(root, query, criteriaBuilder)).isNull();
    verifyNoInteractions(root, criteriaBuilder);
  }

  @Test
  void nameContainsWhenValuePresent_thenBuildsLikePredicateWithLowercase() {
    Specification<Product> specification = ProductSpecification.nameContains("PhOnE");

    when(root.<String>get("name")).thenReturn(namePath);
    when(criteriaBuilder.lower(namePath)).thenReturn(lowerNameExpression);
    when(criteriaBuilder.like(lowerNameExpression, "%phone%")).thenReturn(predicate);

    Predicate result = specification.toPredicate(root, query, criteriaBuilder);

    assertThat(result).isEqualTo(predicate);
    verify(root).get("name");
    verify(criteriaBuilder).lower(namePath);
    verify(criteriaBuilder).like(lowerNameExpression, "%phone%");
  }

  @Test
  void nameContainsWhenNullOrBlank_thenReturnsNullPredicate() {
    Specification<Product> nullSpecification = ProductSpecification.nameContains(null);
    Specification<Product> blankSpecification = ProductSpecification.nameContains("   ");

    assertThat(nullSpecification.toPredicate(root, query, criteriaBuilder)).isNull();
    assertThat(blankSpecification.toPredicate(root, query, criteriaBuilder)).isNull();
    verifyNoInteractions(root, criteriaBuilder);
  }

  @Test
  void isActiveWhenValuePresent_thenBuildsActivePredicate() {
    Specification<Product> specification = ProductSpecification.isActive(true);

    when(root.<Boolean>get("active")).thenReturn(activePath);
    when(criteriaBuilder.equal(activePath, true)).thenReturn(predicate);

    Predicate result = specification.toPredicate(root, query, criteriaBuilder);

    assertThat(result).isEqualTo(predicate);
    verify(root).get("active");
    verify(criteriaBuilder).equal(activePath, true);
  }
}
