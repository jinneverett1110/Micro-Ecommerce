package quant.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sepay")
public class SePaYConfig {
    private String apiKey;
    private String webhookSecret;
    private String bankAccount;
    private String bankName;
    private String accountName;
    private String qrUrl;
}