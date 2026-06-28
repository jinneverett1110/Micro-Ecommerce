// OrderEventConsumer.java
package quant.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import quant.paymentservice.event.OrderCreatedEvent;
import quant.paymentservice.service.PaymentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order.created", groupId = "payment-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received ORDER_CREATED for orderId: {}", event.getOrderId());
        paymentService.processPayment(event);
        // QrResponse được trả về nhưng không cần dùng ở đây
        // Client sẽ poll GET /api/payments/orders/{orderId}/qr để lấy QR
    }
}