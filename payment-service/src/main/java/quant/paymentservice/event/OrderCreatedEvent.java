// OrderCreatedEvent.java
package quant.paymentservice.event;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
    private ShippingAddressEvent shippingAddress;
    private Instant createdAt;

    @Data
    public static class OrderItemEvent {
        private String variantId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        private String imageUrl;
    }

    @Data
    public static class ShippingAddressEvent {
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String district;
        private String ward;
        private String addressDetail;
    }
}