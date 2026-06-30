package quant.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.notificationservice.dto.NotificationMessage;
import quant.notificationservice.dto.NotificationResponse;
import quant.notificationservice.entity.Notification;
import quant.notificationservice.event.PaymentResultEvent;
import quant.notificationservice.redis.RedisPublisher;
import quant.notificationservice.repository.NotificationRepository;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final RedisPublisher redisPublisher;

    @Transactional
    public void handlePaymentSuccess(PaymentResultEvent event) {
        // 1. Lưu notification vào DB
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .type(Notification.Type.PAYMENT_SUCCESS)
                .title("Thanh toán thành công")
                .message("Đơn hàng " + event.getOrderId() + " đã được thanh toán thành công")
                .orderId(event.getOrderId())
                .build();
        notificationRepository.save(notification);

        // 2. Gửi email
        emailService.sendEmail(
                event.getUserEmail(),
                "✅ Thanh toán thành công - Đơn hàng " + event.getOrderId(),
                "payment-success",
                Map.of(
                        "userEmail", event.getUserEmail(),
                        "orderId", event.getOrderId(),
                        "totalAmount", event.getTotalAmount()
                )
        );

        // 3. Push realtime qua WebSocket
        redisPublisher.publish(event.getUserId(), NotificationMessage.builder()
                .id(notification.getId())
                .userId(event.getUserId())
                .type(Notification.Type.PAYMENT_SUCCESS.name())
                .title("Thanh toán thành công")
                .message("Đơn hàng " + event.getOrderId() + " đã được thanh toán thành công")
                .orderId(event.getOrderId())
                .createdAt(Instant.now())
                .build());

        log.info("Handled PAYMENT_SUCCESS for orderId: {}", event.getOrderId());
    }

    @Transactional
    public void handlePaymentFailed(PaymentResultEvent event) {
        // 1. Lưu notification vào DB
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .type(Notification.Type.PAYMENT_FAILED)
                .title("Thanh toán thất bại")
                .message("Đơn hàng " + event.getOrderId() + " thanh toán thất bại")
                .orderId(event.getOrderId())
                .build();
        notificationRepository.save(notification);

        // 2. Gửi email
        emailService.sendEmail(
                event.getUserEmail(),
                "❌ Thanh toán thất bại - Đơn hàng " + event.getOrderId(),
                "payment-failed",
                Map.of(
                        "userEmail", event.getUserEmail(),
                        "orderId", event.getOrderId(),
                        "reason", event.getReason() != null ? event.getReason() : "Không xác định"
                )
        );

        // 3. Push realtime qua WebSocket
        redisPublisher.publish(event.getUserId(), NotificationMessage.builder()
                .id(notification.getId())
                .userId(event.getUserId())
                .type(Notification.Type.PAYMENT_FAILED.name())
                .title("Thanh toán thất bại")
                .message("Đơn hàng " + event.getOrderId() + " thanh toán thất bại")
                .orderId(event.getOrderId())
                .createdAt(Instant.now())
                .build());

        log.info("Handled PAYMENT_FAILED for orderId: {}", event.getOrderId());
    }

    public Page<NotificationResponse> getMyNotifications(String userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public long countUnread(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .orderId(n.getOrderId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}