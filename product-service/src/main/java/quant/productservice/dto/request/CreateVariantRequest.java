// CreateVariantRequest.java
package quant.productservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateVariantRequest {
    @NotBlank(message = "SKU không được để trống")
    private String sku;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stock;

    private List<String> attributeValueIds; // ["uuid-size-S", "uuid-color-red"]
}