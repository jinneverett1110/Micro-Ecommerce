package quant.productservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quant.productservice.dto.request.CreateAttributeTypeRequest;
import quant.productservice.dto.request.CreateAttributeValueRequest;
import quant.productservice.dto.response.ApiResponse;
import quant.productservice.dto.response.AttributeTypeResponse;
import quant.productservice.dto.response.AttributeValueResponse;
import quant.productservice.service.AttributeService;

import java.util.List;

@RestController
@RequestMapping("/api/products/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    @PostMapping("/types")
    public ResponseEntity<ApiResponse<AttributeTypeResponse>> createType(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateAttributeTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(attributeService.createType(request)));
    }

    @PostMapping("/values")
    public ResponseEntity<ApiResponse<AttributeValueResponse>> createValue(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateAttributeValueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(attributeService.createValue(request)));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<AttributeTypeResponse>>> getAllTypes() {
        return ResponseEntity.ok(ApiResponse.success(attributeService.getAllTypes()));
    }
}