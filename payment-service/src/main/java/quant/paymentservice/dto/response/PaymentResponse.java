// PaymentResponse.java
package quant.paymentservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentResponse {
    private String id;
    private String orderId;
    private String userId;
    private String userEmail;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
}