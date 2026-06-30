// PaymentEventConsumer.java
package quant.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import quant.notificationservice.event.PaymentResultEvent;
import quant.notificationservice.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment.success", groupId = "notification-service")
    public void handlePaymentSuccess(PaymentResultEvent event) {
        log.info("Received PAYMENT_SUCCESS for orderId: {}", event.getOrderId());
        notificationService.handlePaymentSuccess(event);
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service")
    public void handlePaymentFailed(PaymentResultEvent event) {
        log.info("Received PAYMENT_FAILED for orderId: {}", event.getOrderId());
        notificationService.handlePaymentFailed(event);
    }
}