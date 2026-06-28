// NotificationMessage.java
package quant.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationMessage {
    private String id;
    private String userId;
    private String type;
    private String title;
    private String message;
    private String orderId;
    private Instant createdAt;
}