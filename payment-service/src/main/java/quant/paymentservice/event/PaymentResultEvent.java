// PaymentResultEvent.java
package quant.paymentservice.event;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentResultEvent {
    private String paymentId;
    private String orderId;
    private String userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String reason;
    private Instant paidAt;
}