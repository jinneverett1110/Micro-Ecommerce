// CreateAttributeTypeRequest.java
package quant.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAttributeTypeRequest {
    @NotBlank(message = "Tên loại thuộc tính không được để trống")
    private String name;
}