package com.grab.store.workflows.internal.workflows.updateproductvariant;

import com.inventory.domain.enums.AdjustmentReason;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductVariantContext(
        String merchantId,
        String createdBy,
        String scopeKey,
        String scopeId,
        String productId,
        String variantId,
        String sku,
        Price price,
        AdjustStock adjustStock,
        String catalogSku,
        PricePair pricePair,
        String createdPriceSetId,
        String inventoryItemId
) {

    public UpdateProductVariantContext {
        if (price != null) {
            price = new Price(
                    price.title(),
                    price.currencyCode(),
                    price.amount(),
                    price.minQuantity(),
                    price.maxQuantity(),
                    price.rules() == null ? List.of() : List.copyOf(price.rules())
            );
        }
    }

    public static UpdateProductVariantContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            String productId,
            String variantId,
            String sku,
            Price price,
            AdjustStock adjustStock
    ) {
        return new UpdateProductVariantContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                variantId,
                sku,
                price,
                adjustStock,
                null,
                null,
                null,
                null
        );
    }

    public UpdateProductVariantContext withVariantUpdated(String newSku) {
        return new UpdateProductVariantContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                variantId,
                sku,
                price,
                adjustStock,
                newSku,
                pricePair,
                createdPriceSetId,
                inventoryItemId
        );
    }

    public UpdateProductVariantContext withPricePair(PricePair newPricePair, boolean created) {
        String nextCreated = createdPriceSetId;
        if (created && newPricePair != null && newPricePair.priceSetId() != null && !newPricePair.priceSetId().isBlank()) {
            nextCreated = newPricePair.priceSetId();
        }
        return new UpdateProductVariantContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                variantId,
                sku,
                price,
                adjustStock,
                catalogSku,
                newPricePair,
                nextCreated,
                inventoryItemId
        );
    }

    public UpdateProductVariantContext withInventoryItem(String newInventoryItemId) {
        return new UpdateProductVariantContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                variantId,
                sku,
                price,
                adjustStock,
                catalogSku,
                pricePair,
                createdPriceSetId,
                newInventoryItemId
        );
    }

    public boolean hasPrice() {
        return price != null;
    }

    public boolean hasAdjustStock() {
        return adjustStock != null;
    }

    public String skuForPrice() {
        if (catalogSku != null && !catalogSku.isBlank()) {
            return catalogSku;
        }
        return sku;
    }

    public record Price(
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            List<PriceRule> rules
    ) {
        public Price {
            rules = rules == null ? List.of() : List.copyOf(rules);
        }
    }

    public record PriceRule(
            String attribute,
            String value,
            String operator,
            Integer priority
    ) {
    }

    public record AdjustStock(
            String inventoryItemId,
            int newOnHandQuantity,
            AdjustmentReason reason
    ) {
    }

    public record PricePair(String variantId, String sku, String priceSetId) {
    }
}
