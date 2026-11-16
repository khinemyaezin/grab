package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.ProductModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.ProductSummaryModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.VariantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductStatusRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductSummaryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerProductCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductCommandService productCommandService;
    private final SellerProductQueryService productQueryService;
    private final ProductModelAssembler productModelAssembler;
    private final ProductSummaryModelAssembler productSummaryModelAssembler;
    private final VariantModelAssembler variantModelAssembler;

    @PostMapping
    @Operation(description = "HAL relation create-product creates a product. Follow get-product in the result to retrieve it.")
    public ResponseEntity<EntityModel<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        ProductResponse response = productCommandService.createProduct(principal.requiredScopeId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(productModelAssembler.toCommandModel(response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<EntityModel<ProductResponse>> getProduct(
            @PathVariable String productId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        return productQueryService.getProduct(principal.requiredScopeId(), productId)
                .map(productModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<EntityModel<ProductResponse>> getProductBySlug(@PathVariable String slug) {
        return productQueryService.getProductBySlug(slug)
                .map(productModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(description = "HAL discovery uses list-products for the default collection and search-products for filtered queries.")
    public ResponseEntity<PagedModel<EntityModel<ProductSummaryResponse>>> searchProducts(
            @AuthenticationPrincipal SellerSecurityPrincipal principal,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProductSummaryResponse> productPage = productQueryService.searchProducts(
                principal.requiredScopeId(), query, page, size);
        return ResponseEntity.ok(productSummaryModelAssembler.toPageModel(productPage, query, page, size));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<EntityModel<ProductResponse>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        ProductResponse response = productCommandService.updateProduct(
                principal.requiredScopeId(), productId, request);
        return ResponseEntity.ok(productModelAssembler.toCommandModel(response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String productId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        productCommandService.deleteProduct(principal.requiredScopeId(), productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<EntityModel<ProductResponse>> updateProductStatus(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductStatusRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        ProductResponse response = productCommandService.updateProductStatus(
                principal.requiredScopeId(), productId, request);
        return ResponseEntity.ok(productModelAssembler.toCommandModel(response));
    }

    @GetMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<EntityModel<VariantResponse>> getVariant(
            @PathVariable String productId,
            @PathVariable String variantId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        return productQueryService.getVariant(principal.requiredScopeId(), productId, variantId)
                .map(v -> variantModelAssembler.toModel(v, productId))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
