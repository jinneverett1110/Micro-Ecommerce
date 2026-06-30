package quant.productservice.dto.request;

import lombok.Data;

@Data
public class SaveImageRequest {
    private String url;
    private Boolean isPrimary = false;
    private Integer sortOrder = 0;
    private String variantId;
}