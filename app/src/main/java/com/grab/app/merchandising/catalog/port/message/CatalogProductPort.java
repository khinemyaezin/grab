package com.grab.app.merchandising.catalog.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Product capabilities.
 * Replaces the legacy Product REST Controller and internal product service data exchange.
 */
public interface CatalogProductPort {

    ProductResult createProduct(CreateProductMessage message);

    ProductResult updateProduct(Id merchantId, Id productId, UpdateProductMessage message);

    void deleteProduct(Id merchantId, Id productId);

    ProductResult updateProductStatus(Id merchantId, Id productId, String status);

    Optional<ProductResult> findProductById(Id merchantId, Id productId);

    Optional<ProductResult> findProductBySlug(String slug);

    List<ProductSummaryResult> searchProducts(ProductSearchCriteria criteria);

    Optional<VariantResult> findVariantById(Id merchantId, Id productId, Id variantId);

    record CreateProductMessage(
            Id merchantId,
            String name,
            Id categoryId,
            String condition,
            String slug,
            String status,
            List<CreateVariantMessage> variants
    ) {}

    record CreateVariantMessage(
            String sku,
            boolean manageInventory
    ) {}

    record UpdateProductMessage(
            String name,
            Id categoryId,
            String condition,
            String slug
    ) {}

    record ProductSearchCriteria(
            Id merchantId,
            String query,
            String variantStatus,
            Id categoryId,
            String productStatus,
            int page,
            int size
    ) {}

    record ProductResult(
            String id,
            String name,
            String categoryId,
            String categoryName,
            String condition,
            String status,
            String slug,
            List<VariantResult> variants
    ) {}

    record VariantResult(
            String id,
            String sku,
            String status,
            String matrixKey,
            boolean manageInventory
    ) {}

    record ProductSummaryResult(
            String productId,
            String productName,
            String status,
            String slug,
            String categoryName,
            String categoryId
    ) {}
}
