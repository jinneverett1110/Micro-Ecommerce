// SePayWebhookRequest.java
package quant.paymentservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SePayWebhookRequest {
    private String id;

    @JsonProperty("gateway")
    private String gateway;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("code")
    private String code;  // Nội dung chuyển khoản

    @JsonProperty("content")
    private String content;

    @JsonProperty("transferType")
    private String transferType;  // in / out

    @JsonProperty("transferAmount")
    private BigDecimal transferAmount;

    @JsonProperty("accumulated")
    private BigDecimal accumulated;

    @JsonProperty("subAccount")
    private String subAccount;

    @JsonProperty("referenceCode")
    private String referenceCode;

    @JsonProperty("description")
    private String description;
}