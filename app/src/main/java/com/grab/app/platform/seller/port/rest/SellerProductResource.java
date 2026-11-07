package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductStatusRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductSummaryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantOptionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantTypeResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Seller Central Catalog & Product management.
 */
public interface SellerProductResource {

    ResponseEntity<EntityModel<ProductResponse>> createProduct(CreateProductRequest request, String merchantId);

    ResponseEntity<EntityModel<ProductResponse>> getProduct(String productId, String merchantId);

    ResponseEntity<EntityModel<ProductResponse>> getProductBySlug(String slug);

    ResponseEntity<PagedModel<EntityModel<ProductSummaryResponse>>> searchProducts(String merchantId, String query, int page, int size);

    ResponseEntity<EntityModel<ProductResponse>> updateProduct(String productId, UpdateProductRequest request, String merchantId);

    ResponseEntity<Void> deleteProduct(String productId, String merchantId);

    ResponseEntity<EntityModel<ProductResponse>> updateProductStatus(String productId, UpdateProductStatusRequest request, String merchantId);

    ResponseEntity<EntityModel<VariantResponse>> getVariant(String productId, String variantId, String merchantId);

    /**
     * Public REST Contract for Product Variant definitions and options.
     */
    interface VariantOptions {
        ResponseEntity<CollectionModel<EntityModel<VariantOptionResponse>>> listVariantOptions(String name, String typeId);
        ResponseEntity<CollectionModel<EntityModel<VariantTypeResponse>>> listVariantTypes(String name);
    }
}
