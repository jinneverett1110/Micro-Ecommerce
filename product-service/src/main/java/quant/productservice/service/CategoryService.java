package quant.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.productservice.dto.request.CreateCategoryRequest;
import quant.productservice.dto.response.CategoryResponse;
import quant.productservice.entity.Category;
import quant.productservice.exception.AppException;
import quant.productservice.exception.ErrorCode;
import quant.productservice.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug()))
            throw new AppException(ErrorCode.CATEGORY_SLUG_EXISTED);

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            category.setParent(parent);
        }

        return toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findRootCategories()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse getById(String id) {
        return toResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(category.getChildren().stream().map(this::toResponse).toList())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}