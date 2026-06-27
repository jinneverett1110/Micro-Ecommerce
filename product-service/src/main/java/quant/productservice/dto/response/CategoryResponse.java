// CategoryResponse.java
package quant.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private String id;
    private String name;
    private String slug;
    private String parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private Instant createdAt;
    private Instant updatedAt;
}