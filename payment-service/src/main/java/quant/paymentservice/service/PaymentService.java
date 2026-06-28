package quant.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        // Idempotency check - tránh xử lý 2 lần
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Payment already exists for orderId: {}", event.getOrderId());
            return;
        }

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .userEmail(event.getUserEmail())
                .amount(event.getTotalAmount())
                .status(Payment.Status.PENDING)
                .build();

        paymentRepository.save(payment);

        // Mock xử lý thanh toán
        // Thực tế sẽ gọi VNPay, Momo, Stripe...
        boolean paymentSuccess = mockProcessPayment(event);

        if (paymentSuccess) {
            payment.setStatus(Payment.Status.SUCCESS);
            payment.setPaidAt(Instant.now());
            paymentRepository.save(payment);

            // Publish PAYMENT_SUCCESS
            kafkaTemplate.send("payment.success", event.getOrderId(),
                    PaymentResultEvent.builder()
                            .paymentId(payment.getId())
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .userEmail(event.getUserEmail())
                            .totalAmount(event.getTotalAmount())
                            .paidAt(payment.getPaidAt())
                            .build());

            log.info("Payment SUCCESS for orderId: {}", event.getOrderId());
        } else {
            payment.setStatus(Payment.Status.FAILED);
            payment.setFailureReason("Thanh toán thất bại");
            paymentRepository.save(payment);

            // Publish PAYMENT_FAILED
            kafkaTemplate.send("payment.failed", event.getOrderId(),
                    PaymentResultEvent.builder()
                            .paymentId(payment.getId())
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .userEmail(event.getUserEmail())
                            .totalAmount(event.getTotalAmount())
                            .reason("Thanh toán thất bại")
                            .build());

            log.info("Payment FAILED for orderId: {}", event.getOrderId());
        }
    }

    public PaymentResponse getByOrderId(String orderId, String userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getUserId().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);

        return toResponse(payment);
    }

    // Mock: 90% thành công, 10% thất bại
    private boolean mockProcessPayment(OrderCreatedEvent event) {
        return Math.random() > 0.1;
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
}