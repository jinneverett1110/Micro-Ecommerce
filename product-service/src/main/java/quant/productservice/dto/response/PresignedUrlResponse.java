// PresignedUrlResponse.java
package quant.productservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresignedUrlResponse {
    private String uploadUrl;   // URL để client upload lên S3
    private String fileUrl;     // URL public để lưu vào DB sau khi upload xong
    private String key;         // S3 key
}