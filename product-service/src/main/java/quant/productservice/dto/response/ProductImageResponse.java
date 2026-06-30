// ProductImageResponse.java
package quant.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponse {
    private String id;
    private String url;
    private Boolean isPrimary;
    private Integer sortOrder;
    private String variantId;
}