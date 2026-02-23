package com.grab.store.catalog.internal.api.rest.service;

import com.grab.store.catalog.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.SaveProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.SyncVariantsRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateProductStatusRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.UpdateVariantRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductStatusResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteVariantResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.RestoreVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductFacadeService {

    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;
    private final VariantCommandService variantCommandService;

    public EntityModel<GetProductResponse> getProduct(String productId) {
        return productQueryService.getProduct(productId);
    }

    public EntityModel<ProductCombinationResponse> getProductCombination(ProductCombinationRequest request) {
        return productQueryService.getProductCombination(request);
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

    public EntityModel<ProductSummaryResponse> getFeaturedProducts(int page, int size) {
        return productQueryService.getFeaturedProducts(page, size);
    }
}
