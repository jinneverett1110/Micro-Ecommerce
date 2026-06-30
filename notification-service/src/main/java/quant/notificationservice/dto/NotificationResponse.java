// NotificationResponse.java
package quant.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private String type;
    private String title;
    private String message;
    private String orderId;
    private Boolean isRead;
    private Instant createdAt;
}