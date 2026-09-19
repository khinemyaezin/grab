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
        List<PublicationLine> publicationLines,
        String productId,
        List<VariantRef> variantRefs,
        List<PricePair> pricePairs,
        List<InventoryItemRef> inventoryItems,
        Set<String> compensatedPriceSetIds,
        boolean productDeleted,
        String productStatus,
        Set<String> assertedChannelIds,
        boolean productAsserted,
        Set<String> stockPathCheckedChannelIds,
        Set<String> missingRouteChannelIds,
        List<PublicationPair> writtenPublications,
        Set<String> compensatedPublicationKeys,
        List<MediaLine> medias,
        List<DescriptionLine> descriptions,
        boolean mediasReplaced,
        boolean descriptionsReplaced,
        boolean statusApplied
) {

    public CreateSellableProductContext {
        variantTypes = variantTypes == null ? List.of() : List.copyOf(variantTypes);
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        pricingLines = pricingLines == null ? List.of() : List.copyOf(pricingLines);
        publicationLines = publicationLines == null ? List.of() : List.copyOf(publicationLines);
        variantRefs = variantRefs == null ? List.of() : List.copyOf(variantRefs);
        pricePairs = pricePairs == null ? List.of() : List.copyOf(pricePairs);
        inventoryItems = inventoryItems == null ? List.of() : List.copyOf(inventoryItems);
        compensatedPriceSetIds = compensatedPriceSetIds == null ? Set.of() : Set.copyOf(compensatedPriceSetIds);
        assertedChannelIds = assertedChannelIds == null ? Set.of() : Set.copyOf(assertedChannelIds);
        stockPathCheckedChannelIds = stockPathCheckedChannelIds == null ? Set.of() : Set.copyOf(stockPathCheckedChannelIds);
        missingRouteChannelIds = missingRouteChannelIds == null ? Set.of() : Set.copyOf(missingRouteChannelIds);
        writtenPublications = writtenPublications == null ? List.of() : List.copyOf(writtenPublications);
        compensatedPublicationKeys = compensatedPublicationKeys == null ? Set.of() : Set.copyOf(compensatedPublicationKeys);
        if (medias != null) {
            medias = List.copyOf(medias);
        }
        if (descriptions != null) {
            descriptions = List.copyOf(descriptions);
        }
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
        return createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                List.of()
        );
    }

    public static CreateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            Product product,
            List<VariantType> variantTypes,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines,
            List<PublicationLine> publicationLines
    ) {
        return createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                publicationLines,
                null,
                null
        );
    }

    public static CreateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            Product product,
            List<VariantType> variantTypes,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines,
            List<PublicationLine> publicationLines,
            List<MediaLine> medias,
            List<DescriptionLine> descriptions
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
                publicationLines,
                null,
                List.of(),
                List.of(),
                List.of(),
                Set.of(),
                false,
                null,
                Set.of(),
                false,
                Set.of(),
                Set.of(),
                List.of(),
                Set.of(),
                medias,
                descriptions,
                false,
                false,
                false
        );
    }

    public CreateSellableProductContext withProductCreated(
            String newProductId,
            List<VariantRef> newVariantRefs
    ) {
        return withProductCreated(newProductId, newVariantRefs, productStatus);
    }

    public CreateSellableProductContext withProductCreated(
            String newProductId,
            List<VariantRef> newVariantRefs,
            String status
    ) {
        return copy(
                assignPublicationVariantIds(publicationLines, newVariantRefs),
                newProductId,
                newVariantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                status,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withPricePair(PricePair pricePair) {
        if (pricePairs.stream().anyMatch(existing -> existing.variantId().equals(pricePair.variantId()))) {
            return this;
        }
        List<PricePair> nextPairs = new ArrayList<>(pricePairs);
        nextPairs.add(pricePair);
        return copy(
                publicationLines,
                productId,
                variantRefs,
                nextPairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withInventoryItem(InventoryItemRef inventoryItem) {
        if (inventoryItems.stream().anyMatch(existing -> existing.inventoryItemId().equals(inventoryItem.inventoryItemId()))) {
            return this;
        }
        List<InventoryItemRef> nextItems = new ArrayList<>(inventoryItems);
        nextItems.add(inventoryItem);
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                nextItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withPriceSetCompensated(String priceSetId) {
        Set<String> next = new LinkedHashSet<>(compensatedPriceSetIds);
        next.add(priceSetId);
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                next,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withProductDeleted() {
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                true,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withChannelAsserted(String salesChannelId) {
        Set<String> next = new LinkedHashSet<>(assertedChannelIds);
        next.add(salesChannelId);
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                next,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withProductAsserted() {
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                true,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withStockPathChecked(String salesChannelId, boolean missingRoute) {
        Set<String> nextChecked = new LinkedHashSet<>(stockPathCheckedChannelIds);
        nextChecked.add(salesChannelId);
        Set<String> nextMissing = new LinkedHashSet<>(missingRouteChannelIds);
        if (missingRoute) {
            nextMissing.add(salesChannelId);
        }
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                nextChecked,
                nextMissing,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withPublicationWritten(PublicationPair publication) {
        String key = publicationKey(publication.variantId(), publication.salesChannelId());
        if (writtenPublications.stream().anyMatch(existing -> publicationKey(
                existing.variantId(),
                existing.salesChannelId()
        ).equals(key))) {
            return this;
        }
        List<PublicationPair> nextWritten = new ArrayList<>(writtenPublications);
        nextWritten.add(publication);
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                nextWritten,
                compensatedPublicationKeys
        );
    }

    public CreateSellableProductContext withPublicationCompensated(String variantId, String salesChannelId) {
        Set<String> next = new LinkedHashSet<>(compensatedPublicationKeys);
        next.add(publicationKey(variantId, salesChannelId));
        return copy(
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                next
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

    @JsonIgnore
    public boolean shouldPublish() {
        return "ACTIVE".equals(productStatus) && !publicationLines.isEmpty();
    }

    @JsonIgnore
    public List<String> uniqueSalesChannelIds() {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (PublicationLine line : publicationLines) {
            if (line.salesChannelId() != null && !line.salesChannelId().isBlank()) {
                ids.add(line.salesChannelId());
            }
        }
        return List.copyOf(ids);
    }

    @JsonIgnore
    public boolean allChannelsAsserted() {
        return assertedChannelIds.containsAll(uniqueSalesChannelIds());
    }

    @JsonIgnore
    public boolean allStockPathsChecked() {
        return stockPathCheckedChannelIds.containsAll(uniqueSalesChannelIds());
    }

    @JsonIgnore
    public boolean allPublicationsWritten() {
        return writtenPublications.size() >= publicationLines.size();
    }

    @JsonIgnore
    public boolean allWrittenPublicationsCompensated() {
        return writtenPublications.stream()
                .map(pair -> publicationKey(pair.variantId(), pair.salesChannelId()))
                .allMatch(compensatedPublicationKeys::contains);
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

    public String resolvedVariantId(PublicationLine publicationLine) {
        if (publicationLine.variantId() != null && !publicationLine.variantId().isBlank()) {
            return publicationLine.variantId();
        }
        return variantIdForSku(publicationLine.sku());
    }

    public static String publicationKey(String variantId, String salesChannelId) {
        return variantId + ":" + salesChannelId;
    }

    private List<PublicationLine> assignPublicationVariantIds(
            List<PublicationLine> lines,
            List<VariantRef> refs
    ) {
        if (refs == null || refs.isEmpty()) {
            return lines;
        }
        return lines.stream()
                .map(line -> assignPublicationVariantId(line, refs))
                .toList();
    }

    private PublicationLine assignPublicationVariantId(PublicationLine line, List<VariantRef> refs) {
        VariantRef match = refs.stream()
                .filter(ref -> ref.sku().equals(line.sku()))
                .findFirst()
                .orElse(null);
        if (match == null) {
            return line;
        }
        return line.withVariantId(match.variantId());
    }

    private CreateSellableProductContext copy(
            List<PublicationLine> newPublicationLines,
            String newProductId,
            List<VariantRef> newVariantRefs,
            List<PricePair> newPricePairs,
            List<InventoryItemRef> newInventoryItems,
            Set<String> newCompensatedPriceSetIds,
            boolean newProductDeleted,
            String newProductStatus,
            Set<String> newAssertedChannelIds,
            boolean newProductAsserted,
            Set<String> newStockPathCheckedChannelIds,
            Set<String> newMissingRouteChannelIds,
            List<PublicationPair> newWrittenPublications,
            Set<String> newCompensatedPublicationKeys
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
                newPublicationLines,
                newProductId,
                newVariantRefs,
                newPricePairs,
                newInventoryItems,
                newCompensatedPriceSetIds,
                newProductDeleted,
                newProductStatus,
                newAssertedChannelIds,
                newProductAsserted,
                newStockPathCheckedChannelIds,
                newMissingRouteChannelIds,
                newWrittenPublications,
                newCompensatedPublicationKeys,
                medias,
                descriptions,
                mediasReplaced,
                descriptionsReplaced,
                statusApplied
        );
    }

    @JsonIgnore
    public boolean shouldReplaceMedias() {
        return medias != null;
    }

    @JsonIgnore
    public boolean shouldReplaceDescriptions() {
        return descriptions != null;
    }

    @JsonIgnore
    public boolean shouldApplyStatus() {
        return product != null && product.status() != null && !product.status().isBlank();
    }

    public CreateSellableProductContext withMediasReplaced() {
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                medias,
                descriptions,
                true,
                descriptionsReplaced,
                statusApplied
        );
    }

    public CreateSellableProductContext withDescriptionsReplaced() {
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                productStatus,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                medias,
                descriptions,
                mediasReplaced,
                true,
                statusApplied
        );
    }

    public CreateSellableProductContext withStatusApplied(String status) {
        return new CreateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                product,
                variantTypes,
                inventoryLines,
                pricingLines,
                publicationLines,
                productId,
                variantRefs,
                pricePairs,
                inventoryItems,
                compensatedPriceSetIds,
                productDeleted,
                status,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                medias,
                descriptions,
                mediasReplaced,
                descriptionsReplaced,
                true
        );
    }

    public record Product(
            String name,
            String categoryId,
            String condition,
            String slug,
            String status,
            List<Variant> variants
    ) {
        public Product {
            variants = variants == null ? List.of() : List.copyOf(variants);
        }

        public Product(String name, String categoryId, String condition, String slug, List<Variant> variants) {
            this(name, categoryId, condition, slug, null, variants);
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

    public record PublicationLine(
            String sku,
            String variantId,
            String salesChannelId
    ) {
        public PublicationLine withVariantId(String assignedVariantId) {
            return new PublicationLine(sku, assignedVariantId, salesChannelId);
        }
    }

    public record VariantRef(String variantId, String sku) {
    }

    public record PricePair(String variantId, String sku, String priceSetId) {
    }

    public record InventoryItemRef(String inventoryItemId, String sku, String locationId) {
    }

    public record PublicationPair(String variantId, String sku, String salesChannelId) {
    }

    public record MediaLine(
            String id,
            String storageKey,
            String contentType,
            Integer rank
    ) {
    }

    public record DescriptionLine(
            String id,
            String name,
            String title,
            String description
    ) {
    }
}
