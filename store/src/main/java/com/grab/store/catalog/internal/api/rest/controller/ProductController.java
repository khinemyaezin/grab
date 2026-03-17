package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.dto.request.*;
import com.grab.store.catalog.internal.api.rest.dto.response.*;
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
        EntityModel<GetProductResponse> response = productFacadeService.getStorefrontProduct(productId);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(
            value = "/{productId}",
            method = {RequestMethod.PUT, RequestMethod.PATCH},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<UpdateProductResponse>> updateProduct(@PathVariable("productId") String productId,
                                                                             @Valid @RequestBody UpdateProductRequest request) {
        EntityModel<UpdateProductResponse> response = productFacadeService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}/descriptions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductDescriptionsResponse>> replaceProductDescriptions(
            @PathVariable("productId") String productId,
            @Valid @RequestBody ReplaceProductDescriptionsRequest request) {
        return ResponseEntity.ok(productFacadeService.replaceProductDescriptions(productId, request));
    }

    @PutMapping(value = "/{productId}/media", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductMediaResponse>> replaceProductMedia(
            @PathVariable("productId") String productId,
            @Valid @RequestBody ReplaceProductMediaRequest request) {
        return ResponseEntity.ok(productFacadeService.replaceProductMedia(productId, request));
    }

    @PutMapping(value = "/{productId}/variants/{variantId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UpdateVariantResponse>> updateVariant(@PathVariable("productId") String productId,
                                                                             @PathVariable("variantId") String variantId,
                                                                             @Valid @RequestBody UpdateVariantRequest request) {
        EntityModel<UpdateVariantResponse> response = productFacadeService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}/variants", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<SyncVariantsResponse>> syncVariants(@PathVariable("productId") String productId,
                                                                          @Valid @RequestBody SyncVariantsRequest request) {
        EntityModel<SyncVariantsResponse> response = productFacadeService.syncVariants(productId, request);
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
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        EntityModel<ProductSummaryResponse> response = productFacadeService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{productId}/variants/{variantId}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<RestoreVariantResponse>> restoreVariant(
            @PathVariable("productId") String productId,
            @PathVariable("variantId") String variantId) {
        EntityModel<RestoreVariantResponse> response = productFacadeService.restoreVariant(productId, variantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/slug/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<GetProductBySlugResponse>> getProductBySlug(@PathVariable("slug") String slug) {
        EntityModel<GetProductBySlugResponse> response = productFacadeService.getProductBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/featured", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductSummaryResponse>> getFeaturedProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        EntityModel<ProductSummaryResponse> response = productFacadeService.getFeaturedProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{productId}/submit-review", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductModerationResponse>> submitForReview(
            @PathVariable("productId") String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        return ResponseEntity.ok(productFacadeService.moderateProduct(productId, "SUBMIT_REVIEW", request));
    }

    @PostMapping(value = "/{productId}/approve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductModerationResponse>> approve(
            @PathVariable("productId") String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        return ResponseEntity.ok(productFacadeService.moderateProduct(productId, "APPROVE", request));
    }

    @PostMapping(value = "/{productId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductModerationResponse>> reject(
            @PathVariable("productId") String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        return ResponseEntity.ok(productFacadeService.moderateProduct(productId, "REJECT", request));
    }

    @PostMapping(value = "/{productId}/suspend", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductModerationResponse>> suspend(
            @PathVariable("productId") String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        return ResponseEntity.ok(productFacadeService.moderateProduct(productId, "SUSPEND", request));
    }

    @PostMapping(value = "/{productId}/restore", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductModerationResponse>> restore(
            @PathVariable("productId") String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        return ResponseEntity.ok(productFacadeService.moderateProduct(productId, "RESTORE", request));
    }

    @PostMapping(value = "/bulk/upsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<BulkUpsertProductsResponse>> bulkUpsert(
            @Valid @RequestBody BulkUpsertProductsRequest request) {
        return ResponseEntity.ok(productFacadeService.bulkUpsertProducts(request));
    }

    @GetMapping(value = "/{productId}/audit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProductAuditResponse>> getAudit(@PathVariable("productId") String productId) {
        return ResponseEntity.ok(productFacadeService.getProductAudit(productId));
    }
}
