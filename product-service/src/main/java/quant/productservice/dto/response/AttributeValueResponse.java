// AttributeValueResponse.java
package quant.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttributeValueResponse {
    private String id;
    private String attributeTypeId;
    private String attributeTypeName;
    private String value;
}