// PaymentQrResponse.java
package quant.paymentservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentQrResponse {
    private String orderId;
    private BigDecimal amount;
    private String bankName;
    private String bankAccount;
    private String accountName;
    private String description;  // Nội dung chuyển khoản
    private String qrUrl;        // URL QR code
}