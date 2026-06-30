package quant.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.productservice.dto.request.SaveImageRequest;
import quant.productservice.dto.response.PresignedUrlResponse;
import quant.productservice.dto.response.ProductImageResponse;
import quant.productservice.entity.Product;
import quant.productservice.entity.ProductImage;
import quant.productservice.entity.ProductVariant;
import quant.productservice.exception.AppException;
import quant.productservice.exception.ErrorCode;
import quant.productservice.repository.ProductImageRepository;
import quant.productservice.repository.ProductRepository;
import quant.productservice.repository.ProductVariantRepository;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Presigner s3Presigner;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    public PresignedUrlResponse generatePresignedUrl(String productId, String fileName, String variantId) {
        // Kiểm tra product tồn tại
        productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Tạo key unique trên S3
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String key = "products/" + productId + "/" + UUID.randomUUID() + extension;

        // Tạo presigned URL (có hiệu lực 15 phút)
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest));

        String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

        return PresignedUrlResponse.builder()
                .uploadUrl(presignedRequest.url().toString())
                .fileUrl(fileUrl)
                .key(key)
                .build();
    }

    @Transactional
    public ProductImageResponse saveImage(String productId, SaveImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(request.getUrl())
                .isPrimary(request.getIsPrimary())
                .sortOrder(request.getSortOrder())
                .build();

        if (request.getVariantId() != null) {
            ProductVariant variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            image.setVariant(variant);
        }

        return toResponse(imageRepository.save(image));
    }

    @Transactional
    public void deleteImage(String imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        imageRepository.delete(image);
    }

    private ProductImageResponse toResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .url(image.getUrl())
                .isPrimary(image.getIsPrimary())
                .sortOrder(image.getSortOrder())
                .variantId(image.getVariant() != null ? image.getVariant().getId() : null)
                .build();
    }
}