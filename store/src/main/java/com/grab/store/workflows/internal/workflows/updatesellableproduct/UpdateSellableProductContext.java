package com.grab.store.workflows.internal.workflows.updatesellableproduct;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        List<PricePair> pricePairs,
        List<String> createdPriceSetIds,
        List<String> inventoryItemIds,
        List<String> createdInventoryItemIds,
        boolean productUpdated,
        Set<String> compensatedPriceSetIds
) {

    public UpdateSellableProductContext {
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        pricingLines = pricingLines == null ? List.of() : List.copyOf(pricingLines);
        variantRefs = variantRefs == null ? List.of() : List.copyOf(variantRefs);
        pricePairs = pricePairs == null ? List.of() : List.copyOf(pricePairs);
        createdPriceSetIds = createdPriceSetIds == null ? List.of() : List.copyOf(createdPriceSetIds);
        inventoryItemIds = inventoryItemIds == null ? List.of() : List.copyOf(inventoryItemIds);
        createdInventoryItemIds = createdInventoryItemIds == null ? List.of() : List.copyOf(createdInventoryItemIds);
        compensatedPriceSetIds = compensatedPriceSetIds == null ? Set.of() : Set.copyOf(compensatedPriceSetIds);
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
                List.of(),
                List.of(),
                List.of(),
                false,
                Set.of()
        );
    }

    public UpdateSellableProductContext withProductUpdated(
            String newProductId,
            List<VariantRef> newVariantRefs
    ) {
        List<PricingLine> assignedPricingLines = assignVariantIds(newVariantRefs);
        return copy(
                newProductId,
                assignedPricingLines,
                newVariantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                true,
                compensatedPriceSetIds
        );
    }

    public UpdateSellableProductContext withPricePair(PricePair pricePair, boolean created) {
        if (pricePairs.stream().anyMatch(existing -> existing.variantId().equals(pricePair.variantId()))) {
            return this;
        }
        List<PricePair> nextPairs = new ArrayList<>(pricePairs);
        nextPairs.add(pricePair);
        List<String> nextCreated = new ArrayList<>(createdPriceSetIds);
        if (created && pricePair.priceSetId() != null && !pricePair.priceSetId().isBlank()) {
            nextCreated.add(pricePair.priceSetId());
        }
        return copy(
                productId,
                pricingLines,
                variantRefs,
                nextPairs,
                nextCreated,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds
        );
    }

    public UpdateSellableProductContext withInventoryItem(String inventoryItemId, boolean created) {
        if (inventoryItemId != null && inventoryItemIds.contains(inventoryItemId)) {
            return this;
        }
        List<String> nextIds = new ArrayList<>(inventoryItemIds);
        nextIds.add(inventoryItemId);
        List<String> nextCreated = new ArrayList<>(createdInventoryItemIds);
        if (created && inventoryItemId != null && !inventoryItemId.isBlank()) {
            nextCreated.add(inventoryItemId);
        }
        return copy(
                productId,
                pricingLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                nextIds,
                nextCreated,
                productUpdated,
                compensatedPriceSetIds
        );
    }

    public UpdateSellableProductContext withPriceSetCompensated(String priceSetId) {
        Set<String> next = new LinkedHashSet<>(compensatedPriceSetIds);
        next.add(priceSetId);
        return copy(
                productId,
                pricingLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                next
        );
    }

    @JsonIgnore
    public boolean allPricesSynced() {
        return pricePairs.size() >= pricingLines.size();
    }

    @JsonIgnore
    public boolean allInventoryItemsSynced() {
        return inventoryItemIds.size() >= inventoryLines.size();
    }

    @JsonIgnore
    public boolean allCreatedPriceSetsCompensated() {
        return createdPriceSetIds.stream().allMatch(compensatedPriceSetIds::contains);
    }

    @JsonIgnore
    public boolean isPartiallyApplied() {
        return productUpdated || !pricePairs.isEmpty() || !inventoryItemIds.isEmpty();
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

    public String variantIdForSku(String sku) {
        VariantRef variantRef = variantRefForSku(sku);
        return variantRef == null ? null : variantRef.variantId();
    }

    public String resolvedVariantId(PricingLine pricingLine) {
        if (pricingLine.variantId() != null && !pricingLine.variantId().isBlank()) {
            return pricingLine.variantId();
        }
        VariantRef variantRef = variantRefForSku(pricingLine.sku());
        return variantRef == null ? null : variantRef.variantId();
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

    private UpdateSellableProductContext copy(
            String newProductId,
            List<PricingLine> newPricingLines,
            List<VariantRef> newVariantRefs,
            List<PricePair> newPricePairs,
            List<String> newCreatedPriceSetIds,
            List<String> newInventoryItemIds,
            List<String> newCreatedInventoryItemIds,
            boolean newProductUpdated,
            Set<String> newCompensatedPriceSetIds
    ) {
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                newProductId,
                product,
                inventoryLines,
                newPricingLines,
                newVariantRefs,
                newPricePairs,
                newCreatedPriceSetIds,
                newInventoryItemIds,
                newCreatedInventoryItemIds,
                newProductUpdated,
                newCompensatedPriceSetIds
        );
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
            List<Variation> variations,
            Boolean manageInventory
    ) {
        public Variant {
            variations = variations == null ? List.of() : List.copyOf(variations);
        }

        public Variant(String sku, String matrixKey, List<Variation> variations) {
            this(sku, matrixKey, variations, null);
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
