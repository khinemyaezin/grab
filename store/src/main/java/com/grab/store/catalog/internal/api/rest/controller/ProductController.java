package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.assembler.*;
import com.grab.store.catalog.internal.api.rest.dto.request.*;
import com.grab.store.catalog.internal.api.rest.dto.response.*;
import com.grab.store.catalog.internal.api.rest.service.ProductCommandService;
import com.grab.store.catalog.internal.api.rest.service.ProductQueryService;
import com.grab.store.catalog.internal.api.rest.service.VariantCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final VariantCommandService variantCommandService;

    private final GetProductModelAssembler getProductModelAssembler;
    private final GetProductBySlugModelAssembler getProductBySlugModelAssembler;
    private final ProductCombinationModelAssembler productCombinationModelAssembler;
    private final ProductSummaryModelAssembler productSummaryModelAssembler;
    private final ProductVariantSummaryModelAssembler productVariantSummaryModelAssembler;
    private final UpdateProductModelAssembler updateProductModelAssembler;
    private final DeleteProductModelAssembler deleteProductModelAssembler;
    private final UpdateProductStatusModelAssembler updateProductStatusModelAssembler;
    private final UpdateVariantModelAssembler updateVariantModelAssembler;
    private final DeleteVariantModelAssembler deleteVariantModelAssembler;
    private final RestoreVariantModelAssembler restoreVariantModelAssembler;
    private final SyncVariantsModelAssembler syncVariantsModelAssembler;

    @GetMapping(value = "/{productId}")
    public ResponseEntity<EntityModel<GetProductResponse>> getProduct(@PathVariable String productId) {
        GetProductResponse response = productQueryService.getProduct(productId);
        return ResponseEntity.ok(getProductModelAssembler.toModel(response));
    }

    @PutMapping(value = "/{productId}")
    public ResponseEntity<EntityModel<UpdateProductResponse>> updateProduct(@PathVariable String productId,
                                                                            @Valid @RequestBody UpdateProductRequest request) {
        UpdateProductResponse response = productCommandService.updateProduct(productId, request);
        return ResponseEntity.ok(updateProductModelAssembler.toModel(response));
    }

    @PutMapping(value = "/{productId}/descriptions")
    public ResponseEntity<EntityModel<ProductDescriptionsResponse>> replaceProductDescriptions(
            @PathVariable String productId,
            @Valid @RequestBody ReplaceProductDescriptionsRequest request) {
        ProductDescriptionsResponse response = productCommandService.replaceProductDescriptions(productId, request);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product")));
    }

    @PutMapping(value = "/{productId}/media")
    public ResponseEntity<EntityModel<ProductMediaResponse>> replaceProductMedia(
            @PathVariable String productId,
            @Valid @RequestBody ReplaceProductMediaRequest request) {
        ProductMediaResponse response = productCommandService.replaceProductMedia(productId, request);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product")));
    }

    @PutMapping(value = "/{productId}/variants/{sku}")
    public ResponseEntity<EntityModel<UpdateVariantResponse>> updateVariant(@PathVariable String productId,
                                                                            @PathVariable("sku") String variantId,
                                                                            @Valid @RequestBody UpdateVariantRequest request) {
        UpdateVariantResponse response = variantCommandService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(updateVariantModelAssembler.toModel(response));
    }

    @PutMapping(value = "/{productId}/variants")
    public ResponseEntity<EntityModel<SyncVariantsResponse>> syncVariants(@PathVariable String productId,
                                                                          @Valid @RequestBody SyncVariantsRequest request) {
        SyncVariantsResponse response = variantCommandService.syncVariants(productId, request);
        return ResponseEntity.ok(syncVariantsModelAssembler.toModel(response));
    }

    @PostMapping(value = "/search")
    public ResponseEntity<PagedModel<EntityModel<ProductSearchResponse>>> searchProducts(
            @Valid @RequestBody ProductSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ProductSearchResponse> pagedResourcesAssembler) {

        Page<ProductSearchResponse> response = productQueryService.getProductSummary(request, pageable);
        PagedModel<EntityModel<ProductSearchResponse>> pageModel =
                pagedResourcesAssembler.toModel(response, productSummaryModelAssembler);
        pageModel.add(linkTo(methodOn(ProductController.class)
                .searchProducts(null, null, null))
                .withRel("search-products"));
        pageModel.add(linkTo(methodOn(ProductController.class)
                .searchProductVariants(null, null, null))
                .withRel("search-product-variants"));
        pageModel.add(linkTo(methodOn(ProductController.class)
                .saveProduct(null))
                .withRel("create-product"));

        return ResponseEntity.ok(pageModel);
    }

    @PostMapping(value = "/variants/search")
    public ResponseEntity<PagedModel<EntityModel<ProductVariantSearchResponse>>> searchProductVariants(
            @Valid @RequestBody ProductVariantSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<ProductVariantSearchResponse> pagedResourcesAssembler) {

        Page<ProductVariantSearchResponse> response =
                productQueryService.getProductVariantSummary(request, pageable);
        PagedModel<EntityModel<ProductVariantSearchResponse>> pageModel =
                pagedResourcesAssembler.toModel(response, productVariantSummaryModelAssembler);
        pageModel.add(linkTo(methodOn(ProductController.class)
                .searchProductVariants(null, null, null))
                .withRel("search-product-variants"));
        pageModel.add(linkTo(methodOn(ProductController.class)
                .searchProducts(null, null, null))
                .withRel("search-products"));
        pageModel.add(linkTo(methodOn(ProductController.class)
                .saveProduct(null))
                .withRel("create-product"));

        return ResponseEntity.ok(pageModel);
    }

    @PostMapping(value = "/variation-matrix")
    public ResponseEntity<EntityModel<VariationMatrixResponse>> getVariationMatrix(
            @Valid @RequestBody VariationMatrixRequest request) {
        VariationMatrixResponse response = productQueryService.getMatrixCombination(request);
        return ResponseEntity.ok(productCombinationModelAssembler.toModel(response));
    }

    @PostMapping()
    public ResponseEntity<Void> saveProduct( @Valid @RequestBody SaveProductRequest request) {
        String productId = productCommandService.saveProduct(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping(value = "/{productId}")
    public ResponseEntity<EntityModel<DeleteProductResponse>> deleteProduct(@PathVariable String productId) {
        DeleteProductResponse response = productCommandService.deleteProduct(productId);

        if (response.deleted()) {
            return ResponseEntity.ok(deleteProductModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping(value = "/{productId}/status")
    public ResponseEntity<EntityModel<UpdateProductStatusResponse>> updateProductStatus(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductStatusRequest request) {
        UpdateProductStatusResponse response = productCommandService.updateProductStatus(productId, request);
        return ResponseEntity.ok(updateProductStatusModelAssembler.toModel(response));
    }

    @DeleteMapping(value = "/{productId}/variants/{sku}")
    public ResponseEntity<EntityModel<DeleteVariantResponse>> deleteVariant(
            @PathVariable String productId,
            @PathVariable("sku") String variantId) {
        DeleteVariantResponse response = variantCommandService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(deleteVariantModelAssembler.toModel(response));
    }

    @PostMapping(value = "/{productId}/variants/{sku}/restore")
    public ResponseEntity<EntityModel<RestoreVariantResponse>> restoreVariant(
            @PathVariable String productId,
            @PathVariable("sku") String variantId) {
        RestoreVariantResponse response = variantCommandService.restoreVariant(productId, variantId);
        return ResponseEntity.ok(restoreVariantModelAssembler.toModel(response));
    }

    @GetMapping(value = "/slug/{slug}")
    public ResponseEntity<EntityModel<GetProductBySlugResponse>> getProductBySlug(@PathVariable String slug) {
        GetProductBySlugResponse response = productQueryService.getProductBySlug(slug);
        return ResponseEntity.ok(getProductBySlugModelAssembler.toModel(response));
    }

    @PostMapping(value = "/{productId}/publish")
    public ResponseEntity<EntityModel<ProductModerationResponse>> publish(
            @PathVariable String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        ProductModerationResponse response = productCommandService.moderateProduct(productId, "PUBLISH", request);
        return ResponseEntity.ok(toProductCommandModel(response));
    }

    @PostMapping(value = "/{productId}/suspend")
    public ResponseEntity<EntityModel<ProductModerationResponse>> suspend(
            @PathVariable String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        ProductModerationResponse response = productCommandService.moderateProduct(productId, "SUSPEND", request);
        return ResponseEntity.ok(toProductCommandModel(response));
    }

    @PostMapping(value = "/{productId}/restore")
    public ResponseEntity<EntityModel<ProductModerationResponse>> restore(
            @PathVariable String productId,
            @RequestBody(required = false) ProductModerationRequest request) {
        ProductModerationResponse response = productCommandService.moderateProduct(productId, "RESTORE", request);
        return ResponseEntity.ok(toProductCommandModel(response));
    }

    @PostMapping(value = "/bulk/upsert")
    public ResponseEntity<EntityModel<BulkUpsertProductsResponse>> bulkUpsert(
            @Valid @RequestBody BulkUpsertProductsRequest request) {
        BulkUpsertProductsResponse response = productCommandService.bulkUpsertProducts(request);
        return ResponseEntity.ok(EntityModel.of(response));
    }

    @GetMapping(value = "/{productId}/audit")
    public ResponseEntity<EntityModel<ProductAuditResponse>> getAudit(@PathVariable String productId) {
        ProductAuditResponse response = productQueryService.getProductAudit(productId);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getAudit(response.productId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product")));
    }

    private EntityModel<ProductModerationResponse> toProductCommandModel(ProductModerationResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product"));
    }
}
