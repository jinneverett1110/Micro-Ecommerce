// UpdateProductRequest.java
package quant.productservice.dto.request;

import lombok.Data;

@Data
public class UpdateProductRequest {
    private String name;
    private String slug;
    private String description;
    private String categoryId;
    private String status;
}