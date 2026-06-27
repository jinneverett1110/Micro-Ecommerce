package quant.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quant.productservice.dto.request.SaveImageRequest;
import quant.productservice.dto.response.ApiResponse;
import quant.productservice.dto.response.PresignedUrlResponse;
import quant.productservice.dto.response.ProductImageResponse;
import quant.productservice.service.ImageService;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String productId,
            @RequestParam String fileName,
            @RequestParam(required = false) String variantId) {
        return ResponseEntity.ok(ApiResponse.success(
                imageService.generatePresignedUrl(productId, fileName, variantId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductImageResponse>> saveImage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String productId,
            @RequestBody SaveImageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(imageService.saveImage(productId, request)));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String productId,
            @PathVariable String imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}