// RedisSubscriber.java
package quant.notificationservice.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import quant.notificationservice.dto.NotificationMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationMessage notification = objectMapper.readValue(
                    message.getBody(), NotificationMessage.class);

            // Push về client qua WebSocket
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId(),
                    "/queue/notifications",
                    notification);

            log.info("Pushed notification to userId: {}", notification.getUserId());
        } catch (Exception e) {
            log.error("Error processing redis message: ", e);
        }
    }
}