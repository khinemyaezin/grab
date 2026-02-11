package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductStatusRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateVariantRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductStatusResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import com.grab.store.catalog.internal.api.rest.service.ProductFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFacadeService productFacadeService;

    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<GetProductResponse>> getProduct(@PathVariable("productId") String productId) {
        EntityModel<GetProductResponse> response = productFacadeService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{productId}/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<GetProductResponse>> getProductFull(@PathVariable("productId") String productId) {
        // Delegate to existing getProduct which already returns full product with new fields
        EntityModel<GetProductResponse> response = productFacadeService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UpdateProductResponse>> updateProduct(@PathVariable("productId") String productId,
                                                                             @Valid @RequestBody UpdateProductRequest request) {
        EntityModel<UpdateProductResponse> response = productFacadeService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}/variants/{variantId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UpdateVariantResponse>> updateVariant(@PathVariable("productId") String productId,
                                                                             @PathVariable("variantId") String variantId,
                                                                             @Valid @RequestBody UpdateVariantRequest request) {
        EntityModel<UpdateVariantResponse> response = productFacadeService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/search",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<ProductSummaryResponse>> getProducts(@Valid @RequestBody ProductSummaryRequest request) {
        EntityModel<ProductSummaryResponse> response = productFacadeService.getProductSummary(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/combination",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<ProductCombinationResponse>> getProductVariationCombination(
            @Valid @RequestBody ProductCombinationRequest request) {

        EntityModel<ProductCombinationResponse> response =
                productFacadeService.getProductCombination(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> saveProduct(
            @Valid @RequestBody SaveProductRequest request) {

        String productId = productFacadeService.saveProduct(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<DeleteProductResponse>> deleteProduct(@PathVariable("productId") String productId) {
        EntityModel<DeleteProductResponse> response = productFacadeService.deleteProduct(productId);

        if (response.getContent() != null && response.getContent().deleted()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(value = "/{productId}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UpdateProductStatusResponse>> updateProductStatus(
            @PathVariable("productId") String productId,
            @Valid @RequestBody UpdateProductStatusRequest request) {
        EntityModel<UpdateProductStatusResponse> response = productFacadeService.updateProductStatus(productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{productId}/variants/{variantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<DeleteVariantResponse>> deleteVariant(
            @PathVariable("productId") String productId,
            @PathVariable("variantId") String variantId) {
        EntityModel<DeleteVariantResponse> response = productFacadeService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/category/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductSummaryResponse>> getProductsByCategory(
            @PathVariable("categoryId") String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        EntityModel<ProductSummaryResponse> response = productFacadeService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(response);
    }
}
