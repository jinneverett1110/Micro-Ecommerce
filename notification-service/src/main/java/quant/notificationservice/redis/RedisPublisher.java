// RedisPublisher.java
package quant.notificationservice.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import quant.notificationservice.dto.NotificationMessage;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String userId, NotificationMessage message) {
        redisTemplate.convertAndSend("notification:" + userId, message);
    }
}