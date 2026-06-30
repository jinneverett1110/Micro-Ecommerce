// ErrorCode.java
package quant.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    PAYMENT_NOT_FOUND(404, "Không tìm thấy thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_EXISTS(400, "Đơn hàng đã được thanh toán", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(404, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
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