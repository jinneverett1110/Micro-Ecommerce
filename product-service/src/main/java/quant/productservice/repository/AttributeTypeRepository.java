// AttributeTypeRepository.java
package quant.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quant.productservice.entity.AttributeType;

public interface AttributeTypeRepository extends JpaRepository<AttributeType, String> {
    boolean existsByName(String name);
}