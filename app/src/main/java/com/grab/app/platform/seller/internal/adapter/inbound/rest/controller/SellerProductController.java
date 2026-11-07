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
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerProductCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerProductQueryService;
import com.grab.app.platform.seller.port.rest.SellerProductResource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/seller/v1/products")
public class SellerProductController implements SellerProductResource {

    private final SellerProductCommandService productCommandService;
    private final SellerProductQueryService productQueryService;
    private final ProductModelAssembler productModelAssembler;
    private final ProductSummaryModelAssembler productSummaryModelAssembler;
    private final VariantModelAssembler variantModelAssembler;

    public SellerProductController(
            SellerProductCommandService productCommandService,
            SellerProductQueryService productQueryService,
            ProductModelAssembler productModelAssembler,
            ProductSummaryModelAssembler productSummaryModelAssembler,
            VariantModelAssembler variantModelAssembler) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
        this.productModelAssembler = productModelAssembler;
        this.productSummaryModelAssembler = productSummaryModelAssembler;
        this.variantModelAssembler = variantModelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        ProductResponse response = productCommandService.createProduct(merchantId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(productModelAssembler.toModel(response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<EntityModel<ProductResponse>> getProduct(
            @PathVariable String productId,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        return productQueryService.getProduct(merchantId, productId)
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
    public ResponseEntity<PagedModel<EntityModel<ProductSummaryResponse>>> searchProducts(
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProductSummaryResponse> productPage = productQueryService.searchProducts(merchantId, query, page, size);
        List<EntityModel<ProductSummaryResponse>> items = productPage.getContent().stream()
                .map(productSummaryModelAssembler::toModel)
                .toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, productPage.getTotalElements());
        PagedModel<EntityModel<ProductSummaryResponse>> pagedModel = PagedModel.of(items, metadata);
        return ResponseEntity.ok(pagedModel);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<EntityModel<ProductResponse>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        ProductResponse response = productCommandService.updateProduct(merchantId, productId, request);
        return ResponseEntity.ok(productModelAssembler.toModel(response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String productId,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        productCommandService.deleteProduct(merchantId, productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<EntityModel<ProductResponse>> updateProductStatus(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductStatusRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        ProductResponse response = productCommandService.updateProductStatus(merchantId, productId, request);
        return ResponseEntity.ok(productModelAssembler.toModel(response));
    }

    @GetMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<EntityModel<VariantResponse>> getVariant(
            @PathVariable String productId,
            @PathVariable String variantId,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId) {
        return productQueryService.getVariant(merchantId, productId, variantId)
                .map(v -> variantModelAssembler.toModel(v, productId))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
