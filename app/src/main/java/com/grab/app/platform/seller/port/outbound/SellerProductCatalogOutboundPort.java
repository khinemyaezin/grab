package com.grab.app.platform.seller.port.outbound;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Seller Outbound Port for Product Catalog and Variant operations.
 * Isolates the Seller BFF from core merchandising domain port interfaces.
 */
public interface SellerProductCatalogOutboundPort {

    ProductOutboundResult createProduct(CreateProductRecord record);

    ProductOutboundResult updateProduct(Id merchantId, Id productId, UpdateProductRecord record);

    void deleteProduct(Id merchantId, Id productId);

    ProductOutboundResult updateProductStatus(Id merchantId, Id productId, String status);

    Optional<ProductOutboundResult> findProductById(Id merchantId, Id productId);

    Optional<ProductOutboundResult> findProductBySlug(String slug);

    List<ProductSummaryOutboundResult> searchProducts(ProductSearchCriteriaRecord criteria);

    Optional<VariantOutboundResult> findVariantById(Id merchantId, Id productId, Id variantId);

    List<VariantTypeOutboundResult> listVariantTypes(String name);

    List<VariantOptionOutboundResult> listVariantOptions(String name, String typeId);

    record CreateProductRecord(
            Id merchantId,
            String name,
            Id categoryId,
            String condition,
            String slug,
            String status,
            List<CreateVariantRecord> variants
    ) {}

    record CreateVariantRecord(
            String sku,
            boolean manageInventory
    ) {}

    record UpdateProductRecord(
            String name,
            Id categoryId,
            String condition,
            String slug
    ) {}

    record ProductSearchCriteriaRecord(
            Id merchantId,
            String query,
            String variantStatus,
            Id categoryId,
            String productStatus,
            int page,
            int size
    ) {}

    record ProductOutboundResult(
            String id,
            String name,
            String categoryId,
            String categoryName,
            String condition,
            String status,
            String slug,
            List<VariantOutboundResult> variants
    ) {}

    record VariantOutboundResult(
            String id,
            String sku,
            String status,
            String matrixKey,
            boolean manageInventory
    ) {}

    record ProductSummaryOutboundResult(
            String productId,
            String productName,
            String status,
            String slug,
            String categoryName,
            String categoryId
    ) {}

    record VariantTypeOutboundResult(
            String id,
            String name
    ) {}

    record VariantOptionOutboundResult(
            String id,
            String name,
            String typeId,
            String typeName
    ) {}
}
