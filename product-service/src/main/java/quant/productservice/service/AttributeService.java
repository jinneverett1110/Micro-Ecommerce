package quant.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.productservice.dto.request.CreateAttributeTypeRequest;
import quant.productservice.dto.request.CreateAttributeValueRequest;
import quant.productservice.dto.response.AttributeTypeResponse;
import quant.productservice.dto.response.AttributeValueResponse;
import quant.productservice.entity.AttributeType;
import quant.productservice.entity.AttributeValue;
import quant.productservice.exception.AppException;
import quant.productservice.exception.ErrorCode;
import quant.productservice.repository.AttributeTypeRepository;
import quant.productservice.repository.AttributeValueRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeTypeRepository attributeTypeRepository;
    private final AttributeValueRepository attributeValueRepository;

    @Transactional
    public AttributeTypeResponse createType(CreateAttributeTypeRequest request) {
        if (attributeTypeRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.ATTRIBUTE_TYPE_NOT_FOUND);

        AttributeType type = AttributeType.builder()
                .name(request.getName())
                .build();

        return toTypeResponse(attributeTypeRepository.save(type));
    }

    @Transactional
    public AttributeValueResponse createValue(CreateAttributeValueRequest request) {
        AttributeType type = attributeTypeRepository.findById(request.getAttributeTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ATTRIBUTE_TYPE_NOT_FOUND));

        AttributeValue value = AttributeValue.builder()
                .attributeType(type)
                .value(request.getValue())
                .build();

        return toValueResponse(attributeValueRepository.save(value));
    }

    public List<AttributeTypeResponse> getAllTypes() {
        return attributeTypeRepository.findAll()
                .stream()
                .map(this::toTypeResponse)
                .toList();
    }

    private AttributeTypeResponse toTypeResponse(AttributeType type) {
        return AttributeTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .values(type.getValues().stream().map(this::toValueResponse).toList())
                .build();
    }

    private AttributeValueResponse toValueResponse(AttributeValue value) {
        return AttributeValueResponse.builder()
                .id(value.getId())
                .attributeTypeId(value.getAttributeType().getId())
                .attributeTypeName(value.getAttributeType().getName())
                .value(value.getValue())
                .build();
    }
}