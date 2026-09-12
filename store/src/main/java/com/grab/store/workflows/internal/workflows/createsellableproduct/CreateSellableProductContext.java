package com.grab.store.workflows.internal.workflows.createsellableproduct;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record CreateSellableProductContext(
        String merchantId,
        String createdBy,
        String scopeKey,
        String scopeId,
        Product product,
        List<VariantType> variantTypes,
        List<InventoryLine> inventoryLines,
        List<PricingLine> pricingLines,
        String productId,
        List<VariantRef> variantRefs,
        List<PricePair> pricePairs,
        List<InventoryItemRef> inventoryItems,
        Set<String> compensatedPriceSetIds,
        boolean productDeleted
) {

    public CreateSellableProductContext {
        variantTypes = variantTypes == null ? List.of() : List.copyOf(variantTypes);
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        pricingLines = pricingLines == null ? List.of() : List.copyOf(pricingLines);
        variantRefs = variantRefs == null ? List.of() : List.copyOf(variantRefs);
        pricePairs = pricePairs == null ? List.of() : List.copyOf(pricePairs);
        inventoryItems = inventoryItems == null ? List.of() : List.copyOf(inventoryItems);
        compensatedPriceSetIds = compensatedPriceSetIds == null ? Set.of() : Set.copyOf(compensatedPriceSetIds);
    }

    public static CreateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            Product product,
            List<VariantType> variantTypes,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines
    ) {
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                null,
                List.of(),
                List.of(),
                List.of(),
                Set.of(),
                false
        );
    }

    public CreateSellableProductContext withProductCreated(
            String newProductId,
            List<VariantRef> newVariantRefs
    ) {
        return copy(
                newProductId,
                newVariantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted
        );
    }

    public CreateSellableProductContext withPricePair(PricePair pricePair) {
        if (pricePairs.stream().anyMatch(existing -> existing.variantId().equals(pricePair.variantId()))) {
            return this;
        }
        List<PricePair> nextPairs = new ArrayList<>(pricePairs);
        nextPairs.add(pricePair);
        return copy(
                productId,
                variantRefs,
                nextPairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted
        );
    }

    public CreateSellableProductContext withInventoryItem(InventoryItemRef inventoryItem) {
        if (inventoryItems.stream().anyMatch(existing -> existing.inventoryItemId().equals(inventoryItem.inventoryItemId()))) {
            return this;
        }
        List<InventoryItemRef> nextItems = new ArrayList<>(inventoryItems);
        nextItems.add(inventoryItem);
        return copy(
                productId,
                variantRefs,
                pricePairs,
                nextItems,
                compensatedPriceSetIds,
                productDeleted
        );
    }

    public CreateSellableProductContext withPriceSetCompensated(String priceSetId) {
        Set<String> next = new LinkedHashSet<>(compensatedPriceSetIds);
        next.add(priceSetId);
        return copy(
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                next,
                productDeleted
        );
    }

    public CreateSellableProductContext withProductDeleted() {
        return copy(
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                true
        );
    }

    @JsonIgnore
    public boolean allPricesCreated() {
        return !variantRefs.isEmpty()
                && variantRefs.stream().allMatch(ref -> pricePairs.stream()
                .anyMatch(pair -> pair.variantId().equals(ref.variantId())));
    }

    @JsonIgnore
    public boolean allInventoryItemsCreated() {
        return inventoryLines.stream().allMatch(line -> inventoryItems.stream()
                .anyMatch(item -> item.sku().equals(line.sku()) && item.locationId().equals(line.locationId())));
    }

    @JsonIgnore
    public boolean allPriceSetsCompensated() {
        return pricePairs.stream().allMatch(pair -> compensatedPriceSetIds.contains(pair.priceSetId()));
    }

    @JsonIgnore
    public boolean isProductCompensated() {
        return productId == null || productDeleted;
    }

    @JsonIgnore
    public List<String> inventoryItemIds() {
        return inventoryItems.stream().map(InventoryItemRef::inventoryItemId).toList();
    }

    public PricingLine pricingLineForSku(String sku) {
        return pricingLines.stream()
                .filter(line -> line.sku().equals(sku))
                .findFirst()
                .orElse(null);
    }

    public String variantIdForSku(String sku) {
        return variantRefs.stream()
                .filter(ref -> ref.sku().equals(sku))
                .map(VariantRef::variantId)
                .findFirst()
                .orElse(null);
    }

    private CreateSellableProductContext copy(
            String newProductId,
            List<VariantRef> newVariantRefs,
            List<PricePair> newPricePairs,
            List<InventoryItemRef> newInventoryItems,
            Set<String> newCompensatedPriceSetIds,
            boolean newProductDeleted
    ) {
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                newProductId,
                newVariantRefs,
                newPricePairs,
                newInventoryItems,
                newCompensatedPriceSetIds,
                newProductDeleted
        );
    }

    public record Product(
            String name,
            String categoryId,
            String condition,
            String slug,
            List<Variant> variants
    ) {
        public Product {
            variants = variants == null ? List.of() : List.copyOf(variants);
        }
    }

    public record VariantType(
            String typeId,
            List<VariantOption> options
    ) {
        public VariantType {
            options = options == null ? List.of() : List.copyOf(options);
        }
    }

    public record VariantOption(String optionId) {
    }

    public record Variant(
            String sku,
            List<Variation> variations,
            Boolean manageInventory
    ) {
        public Variant {
            variations = variations == null ? List.of() : List.copyOf(variations);
        }

        public Variant(String sku, List<Variation> variations) {
            this(sku, variations, null);
        }
    }

    public record Variation(
            String optionId,
            String typeId
    ) {
    }

    public record InventoryLine(
            String sku,
            String locationId,
            int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) {
    }

    public record PricingLine(
            String sku,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            List<PriceRule> rules
    ) {
        public PricingLine {
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

    public record VariantRef(String variantId, String sku) {
    }

    public record PricePair(String variantId, String sku, String priceSetId) {
    }

    public record InventoryItemRef(String inventoryItemId, String sku, String locationId) {
    }
}
