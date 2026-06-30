package quant.productservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Product
    PRODUCT_NOT_FOUND(404, "Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    PRODUCT_SLUG_EXISTED(400, "Slug sản phẩm đã tồn tại", HttpStatus.BAD_REQUEST),

    // Variant
    VARIANT_NOT_FOUND(404, "Không tìm thấy biến thể", HttpStatus.NOT_FOUND),
    VARIANT_SKU_EXISTED(400, "SKU đã tồn tại", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(400, "Không đủ hàng trong kho", HttpStatus.BAD_REQUEST),

    // Category
    CATEGORY_NOT_FOUND(404, "Không tìm thấy danh mục", HttpStatus.NOT_FOUND),
    CATEGORY_SLUG_EXISTED(400, "Slug danh mục đã tồn tại", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_LEAF(400, "Danh mục này không thể chứa sản phẩm", HttpStatus.BAD_REQUEST),

    // Attribute
    ATTRIBUTE_TYPE_NOT_FOUND(404, "Không tìm thấy loại thuộc tính", HttpStatus.NOT_FOUND),
    ATTRIBUTE_VALUE_NOT_FOUND(404, "Không tìm thấy giá trị thuộc tính", HttpStatus.NOT_FOUND),

    // Image
    IMAGE_NOT_FOUND(404, "Không tìm thấy ảnh", HttpStatus.NOT_FOUND),
    IMAGE_UPLOAD_FAILED(500, "Upload ảnh thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    // Common
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