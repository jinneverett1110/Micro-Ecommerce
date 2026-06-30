// AttributeTypeResponse.java
package quant.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttributeTypeResponse {
    private String id;
    private String name;
    private List<AttributeValueResponse> values;
}