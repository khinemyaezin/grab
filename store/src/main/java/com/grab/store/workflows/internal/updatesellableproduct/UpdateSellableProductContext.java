package com.grab.store.workflows.internal.updatesellableproduct;

import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record UpdateSellableProductContext(
        String merchantId,
        String createdBy,
        String scopeKey,
        String scopeId,
        String productId,
        Product product,
        List<InventoryLine> inventoryLines,
        List<PricingLine> pricingLines,
        List<VariantRef> variantRefs,
        List<String> addedSkus,
        Set<String> projectedSkus,
        List<PricePair> pricePairs,
        List<String> createdPriceSetIds,
        List<String> inventoryItemIds,
        List<String> createdInventoryItemIds
) {

    public UpdateSellableProductContext {
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        pricingLines = pricingLines == null ? List.of() : List.copyOf(pricingLines);
        variantRefs = variantRefs == null ? List.of() : List.copyOf(variantRefs);
        addedSkus = addedSkus == null ? List.of() : List.copyOf(addedSkus);
        projectedSkus = projectedSkus == null ? Set.of() : Set.copyOf(projectedSkus);
        pricePairs = pricePairs == null ? List.of() : List.copyOf(pricePairs);
        createdPriceSetIds = createdPriceSetIds == null ? List.of() : List.copyOf(createdPriceSetIds);
        inventoryItemIds = inventoryItemIds == null ? List.of() : List.copyOf(inventoryItemIds);
        createdInventoryItemIds = createdInventoryItemIds == null ? List.of() : List.copyOf(createdInventoryItemIds);
    }

    public static UpdateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            String productId,
            Product product,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines
    ) {
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                List.of(),
                List.of(),
                Set.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public UpdateSellableProductContext withProductUpdated(
            String newProductId,
            List<VariantRef> newVariantRefs,
            List<String> newAddedSkus
    ) {
        List<PricingLine> assignedPricingLines = assignVariantIds(newVariantRefs);
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                newProductId,
                product,
                inventoryLines,
                assignedPricingLines,
                newVariantRefs,
                newAddedSkus,
                projectedSkus,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds
        );
    }

    public UpdateSellableProductContext withProjectedSku(String sku) {
        Set<String> nextProjected = new LinkedHashSet<>(projectedSkus);
        nextProjected.add(sku);
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                variantRefs,
                addedSkus,
                nextProjected,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds
        );
    }

    public UpdateSellableProductContext withPricePair(PricePair pricePair, boolean created) {
        List<PricePair> nextPairs = new ArrayList<>(pricePairs);
        nextPairs.add(pricePair);
        List<String> nextCreated = new ArrayList<>(createdPriceSetIds);
        if (created && pricePair.priceSetId() != null && !pricePair.priceSetId().isBlank()) {
            nextCreated.add(pricePair.priceSetId());
        }
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                variantRefs,
                addedSkus,
                projectedSkus,
                nextPairs,
                nextCreated,
                inventoryItemIds,
                createdInventoryItemIds
        );
    }

    public UpdateSellableProductContext withInventoryItem(String inventoryItemId, boolean created) {
        List<String> nextIds = new ArrayList<>(inventoryItemIds);
        nextIds.add(inventoryItemId);
        List<String> nextCreated = new ArrayList<>(createdInventoryItemIds);
        if (created && inventoryItemId != null && !inventoryItemId.isBlank()) {
            nextCreated.add(inventoryItemId);
        }
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                variantRefs,
                addedSkus,
                projectedSkus,
                pricePairs,
                createdPriceSetIds,
                nextIds,
                nextCreated
        );
    }

    public boolean allAddedSkusProjected() {
        return !addedSkus.isEmpty() && projectedSkus.containsAll(addedSkus);
    }

    public boolean allPricesSynced() {
        return pricePairs.size() >= pricingLines.size();
    }

    public boolean allInventoryItemsSynced() {
        return inventoryItemIds.size() >= inventoryLines.size();
    }

    public PricingLine pricingLineForSku(String sku) {
        return pricingLines.stream()
                .filter(line -> line.sku().equals(sku))
                .findFirst()
                .orElse(null);
    }

    public VariantRef variantRefForSku(String sku) {
        return variantRefs.stream()
                .filter(ref -> ref.sku().equals(sku))
                .findFirst()
                .orElse(null);
    }

    private List<PricingLine> assignVariantIds(List<VariantRef> refs) {
        if (refs == null || refs.isEmpty()) {
            return pricingLines;
        }
        return pricingLines.stream()
                .map(line -> assignVariantId(line, refs))
                .toList();
    }

    private PricingLine assignVariantId(PricingLine line, List<VariantRef> refs) {
        VariantRef match = refs.stream()
                .filter(ref -> ref.sku().equals(line.sku()))
                .findFirst()
                .orElse(null);
        if (match == null) {
            return line;
        }
        return line.withVariantId(match.variantId());
    }

    public record Product(
            String name,
            String categoryId,
            String condition,
            String slug,
            VariantSync variantSync
    ) {
    }

    public record VariantSync(
            String intent,
            List<Variant> overrides,
            List<VariantType> variantTypes
    ) {
        public VariantSync {
            overrides = overrides == null ? List.of() : List.copyOf(overrides);
            variantTypes = variantTypes == null ? List.of() : List.copyOf(variantTypes);
        }
    }

    public record Variant(
            String sku,
            String matrixKey,
            List<Variation> variations
    ) {
        public Variant {
            variations = variations == null ? List.of() : List.copyOf(variations);
        }
    }

    public record Variation(
            String typeId,
            String optionId
    ) {
    }

    public record VariantType(
            String typeId,
            List<VariantOption> options
    ) {
        public VariantType {
            options = options == null ? List.of() : List.copyOf(options);
        }
    }

    public record VariantOption(
            String optionId,
            String optionName
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

    public record PricingLine(
            String sku,
            String variantId,
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

        public PricingLine withVariantId(String assignedVariantId) {
            return new PricingLine(
                    sku,
                    assignedVariantId,
                    title,
                    currencyCode,
                    amount,
                    minQuantity,
                    maxQuantity,
                    rules
            );
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
