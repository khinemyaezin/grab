package com.grab.app.workflow.port.message;

import com.grab.app.workflow.port.event.InventorySyncOp;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.AdjustmentReason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Multi-Domain Saga Workflow Execution.
 * Exposes in-process operations for cross-domain orchestration.
 */
public interface WorkflowExecutionPort {

    CreateSellableProductResult startCreateSellableProduct(CreateSellableProductMessage message);

    CreateSellableProductResult getCreateSellableProduct(Id workflowId);

    UpdateSellableProductResult startUpdateSellableProduct(UpdateSellableProductMessage message);

    UpdateSellableProductResult getUpdateSellableProduct(Id workflowId);

    UpdateProductVariantResult startUpdateProductVariant(UpdateProductVariantMessage message);

    UpdateProductVariantResult getUpdateProductVariant(Id workflowId);

    record WorkflowExecutionResult(
            Id workflowId,
            String workflowName,
            String status,
            Id targetEntityId,
            String currentStep,
            Instant startedAt,
            Instant completedAt
    ) {}

    // ==========================================
    // CREATE SELLABLE PRODUCT MESSAGE & RESULT
    // ==========================================

    record CreateSellableProductMessage(
            Id merchantId,
            String actorId,
            String scopeKey,
            String scopeId,
            Product product,
            List<VariantType> variantTypes,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines,
            List<PublicationLine> publicationLines,
            List<MediaLine> medias,
            List<DescriptionLine> descriptions,
            String idempotencyKey
    ) {
        public record Product(
                String name,
                String categoryId,
                String condition,
                String slug,
                String status,
                List<Variant> variants
        ) {}

        public record VariantType(
                String typeId,
                List<VariantOption> options
        ) {}

        public record VariantOption(String optionId) {}

        public record Variant(
                String sku,
                List<Variation> variations,
                Boolean manageInventory
        ) {}

        public record Variation(
                String optionId,
                String typeId
        ) {}

        public record InventoryLine(
                String sku,
                String locationId,
                int initialQuantity,
                Integer safetyStock,
                Integer reorderPoint,
                Integer reorderQuantity,
                Integer maxStock
        ) {}

        public record PricingLine(
                String sku,
                String title,
                String currencyCode,
                BigDecimal amount,
                Integer minQuantity,
                Integer maxQuantity,
                List<PriceRule> rules
        ) {}

        public record PriceRule(
                String attribute,
                String value,
                String operator,
                Integer priority
        ) {}

        public record PublicationLine(
                String sku,
                String salesChannelId
        ) {}

        public record MediaLine(
                String id,
                String storageKey,
                String contentType,
                Integer rank
        ) {}

        public record DescriptionLine(
                String id,
                String name,
                String title,
                String description
        ) {}
    }

    record CreateSellableProductResult(
            Id workflowId,
            String status,
            String currentStep,
            Id productId,
            List<PricePair> pricePairs,
            List<Id> inventoryItemIds,
            String errorMessage,
            Instant updatedAt
    ) {
        public record PricePair(String variantId, String sku, String priceSetId) {}
    }

    // ==========================================
    // UPDATE SELLABLE PRODUCT MESSAGE & RESULT
    // ==========================================

    record UpdateSellableProductMessage(
            Id merchantId,
            String actorId,
            String scopeKey,
            String scopeId,
            Id productId,
            Product product,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines,
            List<PublicationLine> publicationLines,
            List<PublicationLine> unpublishLines,
            List<MediaLine> medias,
            List<DescriptionLine> descriptions,
            String idempotencyKey
    ) {
        public record Product(
                String name,
                String categoryId,
                String condition,
                String slug,
                String status,
                VariantSync variantSync
        ) {}

        public record VariantSync(
                String intent,
                List<Variant> overrides,
                List<VariantType> variantTypes
        ) {}

        public record Variant(
                String sku,
                String matrixKey,
                List<Variation> variations,
                Boolean manageInventory
        ) {}

        public record Variation(
                String typeId,
                String optionId
        ) {}

        public record VariantType(
                String typeId,
                List<VariantOption> options
        ) {}

        public record VariantOption(
                String optionId,
                String optionName
        ) {}

        public record InventoryLine(
                String sku,
                String locationId,
                String inventoryItemId,
                InventorySyncOp op,
                CreateStock create,
                AdjustStock adjust,
                DamageStock damage,
                WriteOffStock writeOff,
                Reorder reorder
        ) {}

        public record CreateStock(
                int initialQuantity,
                Integer safetyStock,
                Integer reorderPoint,
                Integer reorderQuantity,
                Integer maxStock
        ) {}

        public record AdjustStock(
                int newOnHandQuantity,
                AdjustmentReason reason
        ) {}

        public record DamageStock(
                int quantity,
                String notes
        ) {}

        public record WriteOffStock(
                int quantity,
                String reason,
                String notes
        ) {}

        public record Reorder(
                Integer safetyStock,
                Integer reorderPoint,
                Integer reorderQuantity,
                Integer maxStock
        ) {}

        public record PricingLine(
                String sku,
                String variantId,
                String title,
                String currencyCode,
                BigDecimal amount,
                Integer minQuantity,
                Integer maxQuantity,
                List<PriceRule> rules
        ) {}

        public record PriceRule(
                String attribute,
                String value,
                String operator,
                Integer priority
        ) {}

        public record PublicationLine(
                String sku,
                String salesChannelId
        ) {}

        public record MediaLine(
                String id,
                String storageKey,
                String contentType,
                Integer rank
        ) {}

        public record DescriptionLine(
                String id,
                String name,
                String title,
                String description
        ) {}
    }

    record UpdateSellableProductResult(
            Id workflowId,
            String status,
            String currentStep,
            Id productId,
            boolean productUpdated,
            List<PricePair> pricePairs,
            List<Id> inventoryItemIds,
            int compensatedPriceSetCount,
            List<PublicationPair> writtenPublications,
            List<String> missingRouteChannelIds,
            boolean partiallyApplied,
            String errorMessage
    ) {
        public record PricePair(String variantId, String sku, String priceSetId) {}
        public record PublicationPair(String variantId, String sku, String salesChannelId) {}
    }

    // ==========================================
    // UPDATE PRODUCT VARIANT MESSAGE & RESULT
    // ==========================================

    record UpdateProductVariantMessage(
            Id merchantId,
            String actorId,
            String scopeKey,
            String scopeId,
            Id productId,
            Id variantId,
            String sku,
            Boolean manageInventory,
            Price price,
            List<InventoryLine> inventoryLines,
            String idempotencyKey
    ) {
        public record Price(
                String title,
                String currencyCode,
                BigDecimal amount,
                Integer minQuantity,
                Integer maxQuantity,
                List<PriceRule> rules
        ) {}

        public record PriceRule(
                String attribute,
                String value,
                String operator,
                Integer priority
        ) {}

        public record InventoryLine(
                String sku,
                String locationId,
                String inventoryItemId,
                InventorySyncOp op,
                CreateStock create,
                AdjustStock adjust,
                DamageStock damage,
                WriteOffStock writeOff,
                Reorder reorder
        ) {}

        public record CreateStock(
                int initialQuantity,
                Integer safetyStock,
                Integer reorderPoint,
                Integer reorderQuantity,
                Integer maxStock
        ) {}

        public record AdjustStock(
                int newOnHandQuantity,
                AdjustmentReason reason
        ) {}

        public record DamageStock(
                int quantity,
                String notes
        ) {}

        public record WriteOffStock(
                int quantity,
                String reason,
                String notes
        ) {}

        public record Reorder(
                Integer safetyStock,
                Integer reorderPoint,
                Integer reorderQuantity,
                Integer maxStock
        ) {}
    }

    record UpdateProductVariantResult(
            Id workflowId,
            String status,
            String currentStep,
            Id productId,
            Id variantId,
            String sku,
            boolean variantUpdated,
            PricePair pricePair,
            List<Id> inventoryItemIds,
            int compensatedPriceSetCount,
            boolean partiallyApplied,
            String errorMessage
    ) {
        public record PricePair(String variantId, String sku, String priceSetId) {}
    }
}
