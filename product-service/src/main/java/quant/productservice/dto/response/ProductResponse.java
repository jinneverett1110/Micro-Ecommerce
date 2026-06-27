// ProductResponse.java
package quant.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String categoryId;
    private String categoryName;
    private String status;
    private List<VariantResponse> variants;
    private List<ProductImageResponse> images;
    private Instant createdAt;
    private Instant updatedAt;
}