package quant.paymentservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quant.paymentservice.dto.request.SePayWebhookRequest;
import quant.paymentservice.dto.response.ApiResponse;
import quant.paymentservice.dto.response.PaymentQrResponse;
import quant.paymentservice.dto.response.PaymentResponse;
import quant.paymentservice.entity.Payment;
import quant.paymentservice.service.PaymentService;
import quant.paymentservice.service.SePayService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SePayService sePayService;

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrderId(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getByOrderId(orderId, userId)));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody SePayWebhookRequest webhook,
            @RequestHeader(value = "X-Sepay-Signature", required = false) String signature,
            HttpServletRequest request) {
        try {
            // Verify signature (bảo mật)
            if (signature != null) {
                String payload = new BufferedReader(
                        new InputStreamReader(request.getInputStream()))
                        .lines()
                        .collect(Collectors.joining());

                if (!sePayService.verifyWebhook(payload, signature)) {
                    log.warn("Invalid webhook signature");
                    return ResponseEntity.status(401).body("Invalid signature");
                }
            }

            paymentService.handleWebhook(webhook);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook error: ", e);
            return ResponseEntity.ok("OK"); // Luôn trả 200 để SePay không retry
        }
    }

    // Thêm vào PaymentController
    @GetMapping("/orders/{orderId}/qr")
    public ResponseEntity<ApiResponse<PaymentQrResponse>> getQr(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        Payment payment = paymentService.getPaymentEntity(orderId, userId);
        PaymentQrResponse qr = sePayService.generateQr(orderId, payment.getAmount());
        return ResponseEntity.ok(ApiResponse.success(qr));
    }
}