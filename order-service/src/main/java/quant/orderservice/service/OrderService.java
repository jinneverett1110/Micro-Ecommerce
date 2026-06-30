package quant.orderservice.service;

import com.example.grpc.product.DeductStockRequest;
import com.example.grpc.product.ProductResponse;
import com.example.grpc.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.orderservice.client.ProductGrpcClient;
import quant.orderservice.client.UserGrpcClient;
import quant.orderservice.dto.request.CreateOrderRequest;
import quant.orderservice.dto.response.OrderResponse;
import quant.orderservice.entity.Order;
import quant.orderservice.entity.OrderItem;
import quant.orderservice.event.OrderCreatedEvent;
import quant.orderservice.exception.AppException;
import quant.orderservice.exception.ErrorCode;
import quant.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductGrpcClient productGrpcClient;
    private final UserGrpcClient userGrpcClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public OrderResponse create(String userId, CreateOrderRequest request) {
        // 1. Lấy thông tin user qua gRPC
        UserResponse user = userGrpcClient.getUser(userId);

        // 2. Kiểm tra stock + lấy thông tin product qua gRPC
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            // Check stock
            boolean available = productGrpcClient.checkStock(
                    itemReq.getVariantId(), itemReq.getQuantity());
            if (!available) throw new AppException(ErrorCode.INSUFFICIENT_STOCK);

            // Lấy thông tin product
            ProductResponse product = productGrpcClient.getProduct(itemReq.getVariantId());

            BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            orderItems.add(OrderItem.builder()
                    .variantId(itemReq.getVariantId())
                    .productName(product.getProductName())
                    .sku(product.getSku())
                    .quantity(itemReq.getQuantity())
                    .price(BigDecimal.valueOf(product.getPrice()))
                    .imageUrl(product.getImageUrl())
                    .build());
        }

        // 3. Tạo order
        CreateOrderRequest.ShippingAddressRequest addr = request.getShippingAddress();
        Order order = Order.builder()
                .userId(userId)
                .userEmail(user.getEmail())
                .status(Order.Status.PENDING)
                .totalAmount(totalAmount)
                .note(request.getNote())
                .receiverName(addr.getReceiverName())
                .receiverPhone(addr.getReceiverPhone())
                .province(addr.getProvince())
                .district(addr.getDistrict())
                .ward(addr.getWard())
                .addressDetail(addr.getAddressDetail())
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);
        orderRepository.save(order);

        // 4. Trừ tồn kho qua gRPC
        DeductStockRequest deductRequest = DeductStockRequest.newBuilder()
                .addAllItems(request.getItems().stream()
                        .map(item -> com.example.grpc.product.DeductStockItem.newBuilder()
                                .setVariantId(item.getVariantId())
                                .setQuantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
        productGrpcClient.deductStock(deductRequest);

        // 5. Publish Kafka event ORDER_CREATED
        OrderCreatedEvent event = buildOrderCreatedEvent(order, user.getEmail());
        kafkaTemplate.send("order.created", order.getId(), event);
        log.info("Published ORDER_CREATED event for orderId: {}", order.getId());

        return toResponse(order);
    }

    public Page<OrderResponse> getMyOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    public OrderResponse getById(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Chỉ cho xem order của chính mình
        if (!order.getUserId().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);

        if (order.getStatus() != Order.Status.PENDING)
            throw new AppException(ErrorCode.ORDER_CANNOT_CANCEL);

        order.setStatus(Order.Status.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    // Kafka consumer gọi khi payment thành công
    @Transactional
    public void updateStatus(String orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(status);
        orderRepository.save(order);
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order, String email) {
        return OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(email)
                .totalAmount(order.getTotalAmount())
                .shippingAddress(OrderCreatedEvent.ShippingAddressEvent.builder()
                        .receiverName(order.getReceiverName())
                        .receiverPhone(order.getReceiverPhone())
                        .province(order.getProvince())
                        .district(order.getDistrict())
                        .ward(order.getWard())
                        .addressDetail(order.getAddressDetail())
                        .build())
                .items(order.getItems().stream()
                        .map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                                .variantId(item.getVariantId())
                                .productName(item.getProductName())
                                .sku(item.getSku())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .imageUrl(item.getImageUrl())
                                .build())
                        .toList())
                .createdAt(Instant.now())
                .build();
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .note(order.getNote())
                .shippingAddress(OrderResponse.ShippingAddressResponse.builder()
                        .receiverName(order.getReceiverName())
                        .receiverPhone(order.getReceiverPhone())
                        .province(order.getProvince())
                        .district(order.getDistrict())
                        .ward(order.getWard())
                        .addressDetail(order.getAddressDetail())
                        .build())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemResponse.builder()
                                .id(item.getId())
                                .variantId(item.getVariantId())
                                .productName(item.getProductName())
                                .sku(item.getSku())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .imageUrl(item.getImageUrl())
                                .build())
                        .toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}