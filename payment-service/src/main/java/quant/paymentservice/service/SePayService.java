package quant.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quant.paymentservice.config.SePaYConfig;
import quant.paymentservice.dto.response.PaymentQrResponse;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SePayService {

    private final SePaYConfig sePaYConfig;

    /**
     * Sinh QR URL để khách chuyển khoản
     * Format: https://qr.sepay.vn/img?bank=...&acc=...&template=compact&amount=...&des=...
     */
    public PaymentQrResponse generateQr(String orderId, BigDecimal amount) {
        // Nội dung chuyển khoản = mã đơn hàng (SePay dùng để nhận diện)
        String description = "ORDER " + orderId;
        String encodedDesc = URLEncoder.encode(description, StandardCharsets.UTF_8);

        String qrUrl = sePaYConfig.getQrUrl()
                + "?bank=" + sePaYConfig.getBankName()
                + "&acc=" + sePaYConfig.getBankAccount()
                + "&template=compact"
                + "&amount=" + amount.longValue()
                + "&des=" + encodedDesc;

        return PaymentQrResponse.builder()
                .orderId(orderId)
                .amount(amount)
                .bankName(sePaYConfig.getBankName())
                .bankAccount(sePaYConfig.getBankAccount())
                .accountName(sePaYConfig.getAccountName())
                .description(description)
                .qrUrl(qrUrl)
                .build();
    }

    /**
     * Verify webhook signature từ SePay
     */
    public boolean verifyWebhook(String payload, String signature) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                    sePaYConfig.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString().equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook: ", e);
            return false;
        }
    }
}