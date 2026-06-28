package quant.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    private List<OrderItemRequest> items;

    private String note;

    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @Data
    public static class OrderItemRequest {
        @NotBlank(message = "Variant ID không được để trống")
        private String variantId;

        @NotNull(message = "Số lượng không được để trống")
        private Integer quantity;
    }

    @Data
    public static class ShippingAddressRequest {
        @NotBlank(message = "Tên người nhận không được để trống")
        private String receiverName;

        @NotBlank(message = "Số điện thoại không được để trống")
        private String receiverPhone;

        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        private String province;

        @NotBlank(message = "Quận/Huyện không được để trống")
        private String district;

        @NotBlank(message = "Phường/Xã không được để trống")
        private String ward;

        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        private String addressDetail;
    }
}