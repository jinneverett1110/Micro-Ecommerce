package quant.orderservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String status;
    private BigDecimal totalAmount;
    private String note;
    private ShippingAddressResponse shippingAddress;
    private List<OrderItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    public static class ShippingAddressResponse {
        private String receiverName;
        private String receiverPhone;
        private String province;
        private String district;
        private String ward;
        private String addressDetail;
    }

    @Data
    @Builder
    public static class OrderItemResponse {
        private String id;
        private String variantId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal price;
        private String imageUrl;
    }
}