package quant.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quant.paymentservice.dto.response.ApiResponse;
import quant.paymentservice.dto.response.PaymentResponse;
import quant.paymentservice.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrderId(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getByOrderId(orderId, userId)));
    }
}