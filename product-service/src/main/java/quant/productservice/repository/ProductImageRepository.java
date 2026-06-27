// ProductImageRepository.java
package quant.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quant.productservice.entity.ProductImage;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    List<ProductImage> findByProductIdOrderBySortOrderAsc(String productId);
    List<ProductImage> findByVariantId(String variantId);
}