// CreateAttributeValueRequest.java
package quant.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAttributeValueRequest {
    @NotNull(message = "Loại thuộc tính không được để trống")
    private String attributeTypeId;

    @NotBlank(message = "Giá trị không được để trống")
    private String value;
}