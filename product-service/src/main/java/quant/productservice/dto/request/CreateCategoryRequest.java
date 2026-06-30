// CreateCategoryRequest.java
package quant.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String parentId;
}