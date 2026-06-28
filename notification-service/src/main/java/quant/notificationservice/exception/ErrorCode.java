package quant.notificationservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    NOTIFICATION_NOT_FOUND(404, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),
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