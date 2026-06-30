package quant.orderservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ORDER_NOT_FOUND(404, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_CANCEL(400, "Không thể hủy đơn hàng ở trạng thái này", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(400, "Không đủ hàng trong kho", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(404, "Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    PRODUCT_SERVICE_UNAVAILABLE(503, "Product service không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    USER_NOT_FOUND(404, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    FORBIDDEN(403, "Không có quyền truy cập", HttpStatus.FORBIDDEN),
    INTERNAL_ERROR(500, "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}