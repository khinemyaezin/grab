package com.grab.store.workflows.internal.createsellableproduct;

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
        Set<String> expectedSkus,
        Set<String> projectedSkus,
        List<VariantRef> variantRefs,
        List<PricePair> pricePairs,
        List<String> inventoryItemIds
) {

    public CreateSellableProductContext {
        variantTypes = variantTypes == null ? List.of() : List.copyOf(variantTypes);
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        pricingLines = pricingLines == null ? List.of() : List.copyOf(pricingLines);
        expectedSkus = expectedSkus == null ? Set.of() : Set.copyOf(expectedSkus);
        projectedSkus = projectedSkus == null ? Set.of() : Set.copyOf(projectedSkus);
        variantRefs = variantRefs == null ? List.of() : List.copyOf(variantRefs);
        pricePairs = pricePairs == null ? List.of() : List.copyOf(pricePairs);
        inventoryItemIds = inventoryItemIds == null ? List.of() : List.copyOf(inventoryItemIds);
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
                Set.of(),
                Set.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public CreateSellableProductContext withProductCreated(
            String newProductId,
            List<String> skus,
            List<VariantRef> newVariantRefs
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
                new LinkedHashSet<>(skus),
                projectedSkus,
                newVariantRefs,
                pricePairs,
                inventoryItemIds
        );
    }

    public CreateSellableProductContext withProjectedSku(String sku) {
        Set<String> nextProjected = new LinkedHashSet<>(projectedSkus);
        nextProjected.add(sku);
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                productId,
                expectedSkus,
                nextProjected,
                variantRefs,
                pricePairs,
                inventoryItemIds
        );
    }

    public CreateSellableProductContext withPricePair(PricePair pricePair) {
        List<PricePair> nextPairs = new ArrayList<>(pricePairs);
        nextPairs.add(pricePair);
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                productId,
                expectedSkus,
                projectedSkus,
                variantRefs,
                nextPairs,
                inventoryItemIds
        );
    }

    public CreateSellableProductContext withInventoryItem(String inventoryItemId) {
        List<String> nextIds = new ArrayList<>(inventoryItemIds);
        nextIds.add(inventoryItemId);
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                productId,
                expectedSkus,
                projectedSkus,
                variantRefs,
                pricePairs,
                nextIds
        );
    }

    public boolean allSkusProjected() {
        return !expectedSkus.isEmpty() && projectedSkus.containsAll(expectedSkus);
    }

    public boolean allPricesCreated() {
        return !variantRefs.isEmpty() && pricePairs.size() >= variantRefs.size();
    }

    public boolean allInventoryItemsCreated() {
        return inventoryItemIds.size() >= inventoryLines.size();
    }

    public PricingLine pricingLineForSku(String sku) {
        return pricingLines.stream()
                .filter(line -> line.sku().equals(sku))
                .findFirst()
                .orElse(null);
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
            List<Variation> variations
    ) {
        public Variant {
            variations = variations == null ? List.of() : List.copyOf(variations);
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
}
