package quant.productservice.grpc;

import com.example.grpc.product.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import quant.productservice.entity.ProductVariant;
import quant.productservice.exception.AppException;
import quant.productservice.service.ProductService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;

    @Override
    public void getProduct(GetProductRequest request,
                           StreamObserver<ProductResponse> responseObserver) {
        try {
            ProductVariant variant = productService.getVariantById(request.getVariantId());
            responseObserver.onNext(ProductResponse.newBuilder()
                    .setVariantId(variant.getId())
                    .setProductId(variant.getProduct().getId())
                    .setProductName(variant.getProduct().getName())
                    .setSku(variant.getSku())
                    .setPrice(variant.getPrice().doubleValue())
                    .setStock(variant.getStock())
                    .setImageUrl(variant.getImageUrl() != null ? variant.getImageUrl() : "")
                    .build());
            responseObserver.onCompleted();
        } catch (AppException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void checkStock(CheckStockRequest request,
                           StreamObserver<CheckStockResponse> responseObserver) {
        try {
            ProductVariant variant = productService.getVariantById(request.getVariantId());
            boolean available = variant.getStock() >= request.getQuantity();
            responseObserver.onNext(CheckStockResponse.newBuilder()
                    .setAvailable(available)
                    .setStock(variant.getStock())
                    .build());
            responseObserver.onCompleted();
        } catch (AppException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deductStock(DeductStockRequest request,
                            StreamObserver<DeductStockResponse> responseObserver) {
        try {
            request.getItemsList().forEach(item ->
                    productService.deductStock(item.getVariantId(), item.getQuantity()));

            responseObserver.onNext(DeductStockResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Trừ tồn kho thành công")
                    .build());
            responseObserver.onCompleted();
        } catch (AppException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}