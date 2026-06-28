package quant.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.paymentservice.dto.request.SePayWebhookRequest;
import quant.paymentservice.dto.response.PaymentQrResponse;
import quant.paymentservice.dto.response.PaymentResponse;
import quant.paymentservice.entity.Payment;
import quant.paymentservice.event.OrderCreatedEvent;
import quant.paymentservice.event.PaymentResultEvent;
import quant.paymentservice.exception.AppException;
import quant.paymentservice.exception.ErrorCode;
import quant.paymentservice.repository.PaymentRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SePayService sePayService;

    @Transactional
    public PaymentQrResponse processPayment(OrderCreatedEvent event) {
        // Idempotency check
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already exists for orderId: {}", event.getOrderId());
            Payment existing = paymentRepository.findByOrderId(event.getOrderId()).get();
            return sePayService.generateQr(event.getOrderId(), existing.getAmount());
        }

        // Tạo payment record với status PENDING
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .userEmail(event.getUserEmail())
                .amount(event.getTotalAmount())
                .status(Payment.Status.PENDING)
                .build();
        paymentRepository.save(payment);

        // Sinh QR để khách chuyển khoản
        return sePayService.generateQr(event.getOrderId(), event.getTotalAmount());
    }

    @Transactional
    public void handleWebhook(SePayWebhookRequest webhook) {
        log.info("Received SePay webhook: {}", webhook);

        // Chỉ xử lý giao dịch tiền vào
        if (!"in".equals(webhook.getTransferType())) {
            log.info("Ignoring outgoing transaction");
            return;
        }

        // Extract orderId từ nội dung chuyển khoản
        // Nội dung: "ORDER abc123-def456-..."
        String content = webhook.getContent();
        if (content == null || !content.contains("ORDER ")) {
            log.warn("Cannot extract orderId from content: {}", content);
            return;
        }

        String orderId = content.replace("ORDER ", "").trim();

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(null);

        if (payment == null) {
            log.warn("Payment not found for orderId: {}", orderId);
            return;
        }

        if (payment.getStatus() != Payment.Status.PENDING) {
            log.warn("Payment already processed for orderId: {}", orderId);
            return;
        }

        // Kiểm tra số tiền khớp không
        if (webhook.getTransferAmount().compareTo(payment.getAmount()) < 0) {
            log.warn("Amount mismatch. Expected: {}, Received: {}",
                    payment.getAmount(), webhook.getTransferAmount());
            // Vẫn có thể chấp nhận nếu muốn, hoặc reject tùy business logic
        }

        // Cập nhật payment thành công
        payment.setStatus(Payment.Status.SUCCESS);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        // Publish PAYMENT_SUCCESS
        kafkaTemplate.send("payment.success", orderId,
                PaymentResultEvent.builder()
                        .paymentId(payment.getId())
                        .orderId(orderId)
                        .userId(payment.getUserId())
                        .userEmail(payment.getUserEmail())
                        .totalAmount(payment.getAmount())
                        .paidAt(payment.getPaidAt())
                        .build());

        log.info("Payment SUCCESS for orderId: {}", orderId);
    }

    public PaymentResponse getByOrderId(String orderId, String userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getUserId().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .userEmail(payment.getUserEmail())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .failureReason(payment.getFailureReason())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    // Thêm vào PaymentService
    public Payment getPaymentEntity(String orderId, String userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getUserId().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);
        return payment;
    }
}