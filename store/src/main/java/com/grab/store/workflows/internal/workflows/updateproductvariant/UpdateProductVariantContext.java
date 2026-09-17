package com.grab.store.workflows.internal.workflows.updateproductvariant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record UpdateProductVariantContext(
        String merchantId,
        String createdBy,
        String scopeKey,
        String scopeId,
        String productId,
        String variantId,
        String sku,
        Boolean manageInventory,
        Price price,
        List<InventoryLine> inventoryLines,
        String catalogSku,
        boolean variantUpdated,
        PricePair pricePair,
        List<String> createdPriceSetIds,
        List<String> inventoryItemIds,
        Set<String> compensatedPriceSetIds
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
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        createdPriceSetIds = createdPriceSetIds == null ? List.of() : List.copyOf(createdPriceSetIds);
        inventoryItemIds = inventoryItemIds == null ? List.of() : List.copyOf(inventoryItemIds);
        compensatedPriceSetIds = compensatedPriceSetIds == null ? Set.of() : Set.copyOf(compensatedPriceSetIds);
    }

    public static UpdateProductVariantContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            String productId,
            String variantId,
            String sku,
            Boolean manageInventory,
            Price price,
            List<InventoryLine> inventoryLines
    ) {
        return new UpdateProductVariantContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                variantId,
                sku,
                manageInventory,
                price,
                inventoryLines,
                null,
                false,
                null,
                List.of(),
                List.of(),
                Set.of()
        );
    }

    public UpdateProductVariantContext withVariantUpdated(String newSku) {
        return copy(
                newSku,
                true,
                pricePair,
                createdPriceSetIds,
                inventoryItemIds,
                compensatedPriceSetIds
        );
    }

    public UpdateProductVariantContext withPricePair(PricePair newPricePair, boolean created) {
        if (pricePair != null) {
            return this;
        }
        List<String> nextCreated = new ArrayList<>(createdPriceSetIds);
        if (created && newPricePair != null && newPricePair.priceSetId() != null && !newPricePair.priceSetId().isBlank()) {
            nextCreated.add(newPricePair.priceSetId());
        }
        return copy(
                catalogSku,
                variantUpdated,
                newPricePair,
                nextCreated,
                inventoryItemIds,
                compensatedPriceSetIds
        );
    }

    public UpdateProductVariantContext withInventoryItem(String inventoryItemId) {
        if (inventoryItemId != null && inventoryItemIds.contains(inventoryItemId)) {
            return this;
        }
        List<String> nextIds = new ArrayList<>(inventoryItemIds);
        nextIds.add(inventoryItemId);
        return copy(
                catalogSku,
                variantUpdated,
                pricePair,
                createdPriceSetIds,
                nextIds,
                compensatedPriceSetIds
        );
    }

    public UpdateProductVariantContext withPriceSetCompensated(String priceSetId) {
        Set<String> next = new LinkedHashSet<>(compensatedPriceSetIds);
        next.add(priceSetId);
        return copy(
                catalogSku,
                variantUpdated,
                pricePair,
                createdPriceSetIds,
                inventoryItemIds,
                next
        );
    }

    @JsonIgnore
    public boolean hasPrice() {
        return price != null;
    }

    @JsonIgnore
    public boolean priceSynced() {
        return !hasPrice() || pricePair != null;
    }

    @JsonIgnore
    public boolean allInventoryItemsSynced() {
        return inventoryLines.isEmpty() || inventoryItemIds.size() >= inventoryLines.size();
    }

    @JsonIgnore
    public boolean allCreatedPriceSetsCompensated() {
        return createdPriceSetIds.stream().allMatch(compensatedPriceSetIds::contains);
    }

    @JsonIgnore
    public boolean isPartiallyApplied() {
        return variantUpdated || pricePair != null || !inventoryItemIds.isEmpty();
    }

    public String skuForPrice() {
        if (catalogSku != null && !catalogSku.isBlank()) {
            return catalogSku;
        }
        return sku;
    }

    private UpdateProductVariantContext copy(
            String newCatalogSku,
            boolean newVariantUpdated,
            PricePair newPricePair,
            List<String> newCreatedPriceSetIds,
            List<String> newInventoryItemIds,
            Set<String> newCompensatedPriceSetIds
    ) {
        return new UpdateProductVariantContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                variantId,
                sku,
                manageInventory,
                price,
                inventoryLines,
                newCatalogSku,
                newVariantUpdated,
                newPricePair,
                newCreatedPriceSetIds,
                newInventoryItemIds,
                newCompensatedPriceSetIds
        );
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

    public record InventoryLine(
            String sku,
            String locationId,
            String inventoryItemId,
            InventorySyncOp op,
            InventorySyncPayload.CreateStock create,
            InventorySyncPayload.AdjustStock adjust,
            InventorySyncPayload.DamageStock damage,
            InventorySyncPayload.WriteOffStock writeOff,
            InventorySyncPayload.Reorder reorder
    ) {
    }

    public record PricePair(String variantId, String sku, String priceSetId) {
    }
}
