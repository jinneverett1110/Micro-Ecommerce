// AttributeValueRepository.java
package quant.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quant.productservice.entity.AttributeValue;

import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, String> {
    List<AttributeValue> findByAttributeTypeId(String attributeTypeId);
}