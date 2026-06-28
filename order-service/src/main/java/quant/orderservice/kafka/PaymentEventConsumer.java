// PaymentEventConsumer.java
package quant.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import quant.orderservice.entity.Order;
import quant.orderservice.event.PaymentResultEvent;
import quant.orderservice.service.OrderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment.success", groupId = "order-service")
    public void handlePaymentSuccess(PaymentResultEvent event) {
        log.info("Received PAYMENT_SUCCESS for orderId: {}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), Order.Status.PAID);
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service")
    public void handlePaymentFailed(PaymentResultEvent event) {
        log.info("Received PAYMENT_FAILED for orderId: {}", event.getOrderId());
        orderService.updateStatus(event.getOrderId(), Order.Status.FAILED);
    }
}