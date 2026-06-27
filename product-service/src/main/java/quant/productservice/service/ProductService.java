package quant.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.productservice.dto.request.CreateProductRequest;
import quant.productservice.dto.request.CreateVariantRequest;
import quant.productservice.dto.request.UpdateProductRequest;
import quant.productservice.dto.request.UpdateVariantRequest;
import quant.productservice.dto.response.ProductResponse;
import quant.productservice.dto.response.VariantResponse;
import quant.productservice.entity.*;
import quant.productservice.exception.AppException;
import quant.productservice.exception.ErrorCode;
import quant.productservice.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ProductImageRepository imageRepository;

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySlug(request.getSlug()))
            throw new AppException(ErrorCode.PRODUCT_SLUG_EXISTED);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Chỉ cho phép gắn product vào leaf category
        if (!category.getChildren().isEmpty())
            throw new AppException(ErrorCode.CATEGORY_NOT_LEAF);

        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .category(category)
                .status(Product.Status.ACTIVE)
                .build();

        productRepository.save(product);

        // Tạo variants
        if (request.getVariants() != null) {
            request.getVariants().forEach(v -> createVariant(product, v));
        }

        return toResponse(productRepository.save(product));
    }

    public Page<ProductResponse> getAll(String categoryId, String keyword, Pageable pageable) {
        return productRepository.findAllWithFilter(categoryId, keyword, pageable)
                .map(this::toResponse);
    }

    public ProductResponse getById(String id) {
        return toResponse(productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND)));
    }

    @Transactional
    public ProductResponse update(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getStatus() != null)
            product.setStatus(Product.Status.valueOf(request.getStatus()));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            if (!category.getChildren().isEmpty())
                throw new AppException(ErrorCode.CATEGORY_NOT_LEAF);
            product.setCategory(category);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setStatus(Product.Status.DELETED);
        productRepository.save(product);
    }

    @Transactional
    public VariantResponse addVariant(String productId, CreateVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toVariantResponse(createVariant(product, request));
    }

    @Transactional
    public VariantResponse updateVariant(String productId, String variantId, UpdateVariantRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (request.getPrice() != null) variant.setPrice(request.getPrice());
        if (request.getStock() != null) variant.setStock(request.getStock());

        return toVariantResponse(variantRepository.save(variant));
    }

    @Transactional
    public void deleteVariant(String productId, String variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
        variantRepository.delete(variant);
    }

    // ── Internal dùng cho gRPC ──────────────────────────────────────

    public ProductVariant getVariantById(String variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
    }

    @Transactional
    public boolean deductStock(String variantId, int quantity) {
        int updated = variantRepository.deductStock(variantId, quantity);
        if (updated == 0) throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        return true;
    }

    // ── Helper ──────────────────────────────────────────────────────

    private ProductVariant createVariant(Product product, CreateVariantRequest request) {
        if (variantRepository.existsBySku(request.getSku()))
            throw new AppException(ErrorCode.VARIANT_SKU_EXISTED);

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        variantRepository.save(variant);

        // Gắn attribute values
        if (request.getAttributeValueIds() != null) {
            request.getAttributeValueIds().forEach(valueId -> {
                AttributeValue attrValue = attributeValueRepository.findById(valueId)
                        .orElseThrow(() -> new AppException(ErrorCode.ATTRIBUTE_VALUE_NOT_FOUND));

                VariantAttribute va = VariantAttribute.builder()
                        .id(new VariantAttribute.VariantAttributeId(variant.getId(), valueId))
                        .variant(variant)
                        .attributeValue(attrValue)
                        .build();

                variant.getAttributes().add(va);
            });
        }

        return variantRepository.save(variant);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .status(product.getStatus().name())
                .variants(product.getVariants().stream().map(this::toVariantResponse).toList())
                .images(imageRepository.findByProductIdOrderBySortOrderAsc(product.getId())
                        .stream()
                        .map(img -> quant.productservice.dto.response.ProductImageResponse.builder()
                                .id(img.getId())
                                .url(img.getUrl())
                                .isPrimary(img.getIsPrimary())
                                .sortOrder(img.getSortOrder())
                                .variantId(img.getVariant() != null ? img.getVariant().getId() : null)
                                .build())
                        .toList())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private VariantResponse toVariantResponse(ProductVariant variant) {
        return VariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stock(variant.getStock())
                .imageUrl(variant.getImageUrl())
                .attributes(variant.getAttributes().stream()
                        .map(va -> quant.productservice.dto.response.AttributeValueResponse.builder()
                                .id(va.getAttributeValue().getId())
                                .attributeTypeId(va.getAttributeValue().getAttributeType().getId())
                                .attributeTypeName(va.getAttributeValue().getAttributeType().getName())
                                .value(va.getAttributeValue().getValue())
                                .build())
                        .toList())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}