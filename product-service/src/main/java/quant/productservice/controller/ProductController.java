package quant.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quant.productservice.dto.request.CreateProductRequest;
import quant.productservice.dto.request.CreateVariantRequest;
import quant.productservice.dto.request.UpdateProductRequest;
import quant.productservice.dto.request.UpdateVariantRequest;
import quant.productservice.dto.response.ApiResponse;
import quant.productservice.dto.response.ProductResponse;
import quant.productservice.dto.response.VariantResponse;
import quant.productservice.service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                productService.getAll(categoryId, keyword, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Variant ─────────────────────────────────────────────────────

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<VariantResponse>> addVariant(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String productId,
            @Valid @RequestBody CreateVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.addVariant(productId, request)));
    }

    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String productId,
            @PathVariable String variantId,
            @RequestBody UpdateVariantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.updateVariant(productId, variantId, request)));
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String productId,
            @PathVariable String variantId) {
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}