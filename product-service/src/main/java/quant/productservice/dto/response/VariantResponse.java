// VariantResponse.java
package quant.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class VariantResponse {
    private String id;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private List<AttributeValueResponse> attributes;
    private Instant createdAt;
    private Instant updatedAt;
}