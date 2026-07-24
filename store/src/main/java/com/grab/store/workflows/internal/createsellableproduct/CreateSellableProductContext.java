package com.grab.store.workflows.internal.createsellableproduct;

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
        String productId,
        Set<String> expectedSkus,
        Set<String> projectedSkus,
        List<String> inventoryItemIds
) {

    public CreateSellableProductContext {
        variantTypes = variantTypes == null ? List.of() : List.copyOf(variantTypes);
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        expectedSkus = expectedSkus == null ? Set.of() : Set.copyOf(expectedSkus);
        projectedSkus = projectedSkus == null ? Set.of() : Set.copyOf(projectedSkus);
        inventoryItemIds = inventoryItemIds == null ? List.of() : List.copyOf(inventoryItemIds);
    }

    public static CreateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            Product product,
            List<VariantType> variantTypes,
            List<InventoryLine> inventoryLines
    ) {
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                null,
                Set.of(),
                Set.of(),
                List.of()
        );
    }

    public CreateSellableProductContext withProductCreated(String newProductId, List<String> skus) {
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                newProductId,
                new LinkedHashSet<>(skus),
                projectedSkus,
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
                productId,
                expectedSkus,
                nextProjected,
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
                productId,
                expectedSkus,
                projectedSkus,
                nextIds
        );
    }

    public boolean allSkusProjected() {
        return !expectedSkus.isEmpty() && projectedSkus.containsAll(expectedSkus);
    }

    public boolean allInventoryItemsCreated() {
        return inventoryItemIds.size() >= inventoryLines.size();
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
}
