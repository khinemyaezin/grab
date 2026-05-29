package com.grab.store.catalog.internal.api.rest.service;

import com.grab.store.catalog.internal.api.rest.dto.request.*;
import com.grab.store.catalog.internal.api.rest.dto.response.*;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacadeService {

    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;
    private final VariantCommandService variantCommandService;

    public EntityModel<GetProductResponse> getProduct(String productId) {
        return productQueryService.getProduct(productId);
    }

    public EntityModel<GetProductResponse> getStorefrontProduct(String productId) {
        return productQueryService.getStorefrontProduct(productId);
    }

    public EntityModel<VariationMatrixResponse> getMatrixCombination(VariationMatrixRequest request) {
        return productQueryService.getMatrixCombination(request);
    }

    public String saveProduct(SaveProductRequest request) {
        return productCommandService.saveProduct(request);
    }

    public EntityModel<ProductSummaryResponse> getProductSummary(ProductSummaryRequest request) {
        return productQueryService.getProductSummary(request);
    }

    public EntityModel<DeleteProductResponse> deleteProduct(String productId) {
        return productCommandService.deleteProduct(productId);
    }

    public EntityModel<UpdateProductResponse> updateProduct(String productId, UpdateProductRequest request) {
        return productCommandService.updateProduct(productId, request);
    }

    public EntityModel<ProductDescriptionsResponse> replaceProductDescriptions(String productId, ReplaceProductDescriptionsRequest request) {
        return productCommandService.replaceProductDescriptions(productId, request);
    }

    public EntityModel<ProductMediaResponse> replaceProductMedia(String productId, ReplaceProductMediaRequest request) {
        return productCommandService.replaceProductMedia(productId, request);
    }

    public EntityModel<UpdateVariantResponse> updateVariant(String productId, String variantId, UpdateVariantRequest request) {
        return variantCommandService.updateVariant(productId, variantId, request);
    }

    public EntityModel<UpdateProductStatusResponse> updateProductStatus(String productId, UpdateProductStatusRequest request) {
        return productCommandService.updateProductStatus(productId, request);
    }

    public EntityModel<DeleteVariantResponse> deleteVariant(String productId, String variantId) {
        return variantCommandService.deleteVariant(productId, variantId);
    }

    public EntityModel<ProductSummaryResponse> getProductsByCategory(String categoryId, int page, int size) {
        return productQueryService.getProductsByCategory(categoryId, page, size);
    }

    public EntityModel<RestoreVariantResponse> restoreVariant(String productId, String variantId) {
        return variantCommandService.restoreVariant(productId, variantId);
    }

    public EntityModel<SyncVariantsResponse> syncVariants(String productId, SyncVariantsRequest request) {
        return variantCommandService.syncVariants(productId, request);
    }

    public EntityModel<GetProductBySlugResponse> getProductBySlug(String slug) {
        return productQueryService.getProductBySlug(slug);
    }

    public EntityModel<ProductModerationResponse> moderateProduct(String productId, String action, ProductModerationRequest request) {
        return productCommandService.moderateProduct(productId, action, request);
    }

    public EntityModel<ProductAuditResponse> getProductAudit(String productId) {
        return productQueryService.getProductAudit(productId);
    }

    public EntityModel<BulkUpsertProductsResponse> bulkUpsertProducts(BulkUpsertProductsRequest request) {
        List<BulkUpsertProductsResponse.Entry> results = new ArrayList<>();
        for (SaveProductRequest productRequest : request.products()) {
            String createdId = productCommandService.saveProduct(productRequest);
            results.add(new BulkUpsertProductsResponse.Entry(createdId, "CREATED"));
        }
        return EntityModel.of(new BulkUpsertProductsResponse(results));
    }
}
