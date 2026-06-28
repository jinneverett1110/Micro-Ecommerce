package quant.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quant.orderservice.dto.request.CreateOrderRequest;
import quant.orderservice.dto.response.ApiResponse;
import quant.orderservice.dto.response.OrderResponse;
import quant.orderservice.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(orderService.create(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(userId, pageable)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(orderId, userId)));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancel(orderId, userId)));
    }
}