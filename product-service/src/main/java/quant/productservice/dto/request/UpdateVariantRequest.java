// UpdateVariantRequest.java
package quant.productservice.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateVariantRequest {
    private BigDecimal price;
    private Integer stock;
}