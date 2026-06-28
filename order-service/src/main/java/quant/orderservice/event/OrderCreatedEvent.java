// OrderCreatedEvent.java
package quant.orderservice.event;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private String userEmail;
    private List<OrderItemEvent> items;
    private BigDecimal totalAmount;
    private ShippingAddressEvent shippingAddress;
    private Instant createdAt;

    @Data
    @Builder
    public static class OrderItemEvent {
        private String variantId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        private String imageUrl;
    }

    @Data
    @Builder
    public static class ShippingAddressEvent {
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String district;
        private String ward;
        private String addressDetail;
    }
}