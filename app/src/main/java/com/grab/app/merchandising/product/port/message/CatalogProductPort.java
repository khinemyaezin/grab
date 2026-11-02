package com.grab.app.merchandising.product.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Java Port Interface for Product capabilities.
 * Replaces the legacy Product REST Controller and internal product service data exchange.
 */
public interface CatalogProductPort {

    ProductResult createProduct(CreateProductMessage message);

    ProductResult updateProduct(Id productId, UpdateProductMessage message);

    void deleteProduct(Id productId);

    ProductResult updateProductStatus(Id productId, String status);

    void publishToChannels(Id productId, List<Id> salesChannelIds);

    void unpublishFromChannels(Id productId, List<Id> salesChannelIds);

    void suspendProduct(Id productId, String reason);

    void restoreProduct(Id productId);

    ProductResult replaceDescriptions(Id productId, List<ProductDescriptionMessage> descriptions);

    ProductResult replaceMedia(Id productId, List<ProductMediaMessage> media);

    Optional<ProductResult> findProductById(Id productId);

    Optional<ProductResult> findProductBySlug(String slug);

    List<ProductSummaryResult> searchProducts(ProductSearchCriteria criteria);

    VariantResult createVariant(Id productId, CreateVariantMessage message);

    VariantResult updateVariant(Id productId, Id variantId, UpdateVariantMessage message);

    void deleteVariant(Id productId, Id variantId);

    void restoreVariant(Id productId, Id variantId);

    Optional<VariantResult> findVariantById(Id productId, Id variantId);

    List<VariantResult> searchVariants(VariantSearchCriteria criteria);

    VariationMatrixResult generateVariationMatrix(Id productId, VariationMatrixRequestMessage message);

    VariantResult batchVariantImages(Id productId, Id variantId, List<VariantImageMessage> images);

    // ==========================================
    // Inbound Message Records
    // ==========================================

    record CreateProductMessage(
            Id merchantId,
            String title,
            String slug,
            String description,
            Id categoryId,
            List<CreateVariantMessage> variants,
            List<ProductMediaMessage> media,
            Map<String, String> attributes
    ) {}

    record UpdateProductMessage(
            String title,
            String description,
            Id categoryId,
            Map<String, String> attributes
    ) {}

    record CreateVariantMessage(
            String sku,
            String title,
            BigDecimal price,
            String currencyCode,
            boolean manageInventory,
            Map<String, String> optionValues
    ) {}

    record UpdateVariantMessage(
            String title,
            BigDecimal price,
            String currencyCode,
            boolean manageInventory,
            Map<String, String> optionValues
    ) {}

    record ProductDescriptionMessage(
            String languageCode,
            String description,
            String bulletPoints
    ) {}

    record ProductMediaMessage(
            String mediaType,
            String url,
            int sortOrder,
            boolean primaryImage
    ) {}

    record VariantImageMessage(
            String url,
            int sortOrder,
            boolean thumbnail
    ) {}

    record ProductSearchCriteria(
            Id merchantId,
            Id categoryId,
            String status,
            String query,
            int page,
            int size
    ) {}

    record VariantSearchCriteria(
            Id productId,
            String sku,
            String status,
            int page,
            int size
    ) {}

    record VariationMatrixRequestMessage(
            List<Id> optionValueIds
    ) {}

    // ==========================================
    // Outbound Result Records
    // ==========================================

    record ProductResult(
            Id id,
            Id merchantId,
            String title,
            String slug,
            String description,
            Id categoryId,
            String status,
            List<VariantResult> variants,
            List<ProductMediaMessage> media,
            Instant createdAt,
            Instant updatedAt
    ) {}

    record VariantResult(
            Id variantId,
            Id productId,
            String sku,
            String title,
            BigDecimal price,
            String currencyCode,
            String status,
            boolean manageInventory,
            Map<String, String> optionValues,
            List<VariantImageMessage> images
    ) {}

    record ProductSummaryResult(
            Id id,
            Id merchantId,
            String title,
            String slug,
            Id categoryId,
            String status,
            int variantCount,
            Instant createdAt
    ) {}

    record VariationMatrixResult(
            Id productId,
            List<VariantResult> generatedVariants
    ) {}
}

