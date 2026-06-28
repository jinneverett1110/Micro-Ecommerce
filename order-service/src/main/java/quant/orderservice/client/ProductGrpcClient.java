// ProductGrpcClient.java
package quant.orderservice.client;

import com.example.grpc.product.*;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import quant.orderservice.exception.AppException;
import quant.orderservice.exception.ErrorCode;

@Slf4j
@Component
public class ProductGrpcClient {

    @GrpcClient("product-service")
    private ProductServiceGrpc.ProductServiceBlockingStub productStub;

    public ProductResponse getProduct(String variantId) {
        try {
            return productStub.getProduct(
                    GetProductRequest.newBuilder()
                            .setVariantId(variantId)
                            .build());
        } catch (StatusRuntimeException e) {
            log.error("gRPC getProduct error: {}", e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    public boolean checkStock(String variantId, int quantity) {
        try {
            CheckStockResponse response = productStub.checkStock(
                    CheckStockRequest.newBuilder()
                            .setVariantId(variantId)
                            .setQuantity(quantity)
                            .build());
            return response.getAvailable();
        } catch (StatusRuntimeException e) {
            log.error("gRPC checkStock error: {}", e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        }
    }

    public void deductStock(DeductStockRequest request) {
        try {
            DeductStockResponse response = productStub.deductStock(request);
            if (!response.getSuccess()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
        } catch (StatusRuntimeException e) {
            log.error("gRPC deductStock error: {}", e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        }
    }
}