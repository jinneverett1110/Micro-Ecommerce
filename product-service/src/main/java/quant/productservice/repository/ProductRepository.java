// ProductRepository.java
package quant.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import quant.productservice.entity.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsBySlug(String slug);
    Optional<Product> findBySlug(String slug);

    @Query("""
            SELECT p FROM Product p
            WHERE p.status = 'ACTIVE'
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> findAllWithFilter(
            @Param("categoryId") String categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}