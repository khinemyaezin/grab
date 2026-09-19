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
        List<PublicationLine> publicationLines,
        List<VariantRef> variantRefs,
        List<PricePair> pricePairs,
        List<String> createdPriceSetIds,
        List<String> inventoryItemIds,
        List<String> createdInventoryItemIds,
        boolean productUpdated,
        Set<String> compensatedPriceSetIds,
        Set<String> assertedChannelIds,
        boolean productAsserted,
        Set<String> stockPathCheckedChannelIds,
        Set<String> missingRouteChannelIds,
        List<PublicationPair> writtenPublications,
        Set<String> compensatedPublicationKeys,
        List<PublicationLine> unpublishLines,
        List<PublicationPair> unpublishedPublications,
        Set<String> compensatedUnpublishKeys,
        List<MediaLine> medias,
        List<DescriptionLine> descriptions,
        boolean mediasReplaced,
        boolean descriptionsReplaced,
        boolean statusApplied
) {

    public UpdateSellableProductContext {
        inventoryLines = inventoryLines == null ? List.of() : List.copyOf(inventoryLines);
        pricingLines = pricingLines == null ? List.of() : List.copyOf(pricingLines);
        publicationLines = publicationLines == null ? List.of() : List.copyOf(publicationLines);
        variantRefs = variantRefs == null ? List.of() : List.copyOf(variantRefs);
        pricePairs = pricePairs == null ? List.of() : List.copyOf(pricePairs);
        createdPriceSetIds = createdPriceSetIds == null ? List.of() : List.copyOf(createdPriceSetIds);
        inventoryItemIds = inventoryItemIds == null ? List.of() : List.copyOf(inventoryItemIds);
        createdInventoryItemIds = createdInventoryItemIds == null ? List.of() : List.copyOf(createdInventoryItemIds);
        compensatedPriceSetIds = compensatedPriceSetIds == null ? Set.of() : Set.copyOf(compensatedPriceSetIds);
        assertedChannelIds = assertedChannelIds == null ? Set.of() : Set.copyOf(assertedChannelIds);
        stockPathCheckedChannelIds = stockPathCheckedChannelIds == null
                ? Set.of()
                : Set.copyOf(stockPathCheckedChannelIds);
        missingRouteChannelIds = missingRouteChannelIds == null ? Set.of() : Set.copyOf(missingRouteChannelIds);
        writtenPublications = writtenPublications == null ? List.of() : List.copyOf(writtenPublications);
        compensatedPublicationKeys = compensatedPublicationKeys == null
                ? Set.of()
                : Set.copyOf(compensatedPublicationKeys);
        unpublishLines = unpublishLines == null ? List.of() : List.copyOf(unpublishLines);
        unpublishedPublications = unpublishedPublications == null
                ? List.of()
                : List.copyOf(unpublishedPublications);
        compensatedUnpublishKeys = compensatedUnpublishKeys == null
                ? Set.of()
                : Set.copyOf(compensatedUnpublishKeys);
        if (medias != null) {
            medias = List.copyOf(medias);
        }
        if (descriptions != null) {
            descriptions = List.copyOf(descriptions);
        }
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
        return createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                List.of()
        );
    }

    public static UpdateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            String productId,
            Product product,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines,
            List<PublicationLine> publicationLines
    ) {
        return createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                publicationLines,
                List.of()
        );
    }

    public static UpdateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            String productId,
            Product product,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines,
            List<PublicationLine> publicationLines,
            List<PublicationLine> unpublishLines
    ) {
        return createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                publicationLines,
                unpublishLines,
                null,
                null
        );
    }

    public static UpdateSellableProductContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            String productId,
            Product product,
            List<InventoryLine> inventoryLines,
            List<PricingLine> pricingLines,
            List<PublicationLine> publicationLines,
            List<PublicationLine> unpublishLines,
            List<MediaLine> medias,
            List<DescriptionLine> descriptions
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
                publicationLines,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                Set.of(),
                Set.of(),
                false,
                Set.of(),
                Set.of(),
                List.of(),
                Set.of(),
                unpublishLines,
                List.of(),
                Set.of(),
                medias,
                descriptions,
                false,
                false,
                false
        );
    }

    public UpdateSellableProductContext withProductUpdated(
            String newProductId,
            List<VariantRef> newVariantRefs
    ) {
        List<PricingLine> assignedPricingLines = assignPricingVariantIds(newVariantRefs);
        List<PublicationLine> assignedPublicationLines = assignPublicationVariantIds(
                publicationLines,
                newVariantRefs
        );
        List<PublicationLine> assignedUnpublishLines = assignResolvableUnpublishLines(
                unpublishLines,
                newVariantRefs
        );
        return copy(
                newProductId,
                assignedPricingLines,
                assignedPublicationLines,
                newVariantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                true,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                assignedUnpublishLines,
                unpublishedPublications,
                compensatedUnpublishKeys
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
                publicationLines,
                variantRefs,
                nextPairs,
                nextCreated,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
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
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                nextIds,
                nextCreated,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public UpdateSellableProductContext withPriceSetCompensated(String priceSetId) {
        Set<String> next = new LinkedHashSet<>(compensatedPriceSetIds);
        next.add(priceSetId);
        return copy(
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                next,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public UpdateSellableProductContext withChannelAsserted(String salesChannelId) {
        Set<String> next = new LinkedHashSet<>(assertedChannelIds);
        next.add(salesChannelId);
        return copy(
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                next,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public UpdateSellableProductContext withProductAsserted() {
        return copy(
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                true,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public UpdateSellableProductContext withStockPathChecked(String salesChannelId, boolean missingRoute) {
        Set<String> nextChecked = new LinkedHashSet<>(stockPathCheckedChannelIds);
        nextChecked.add(salesChannelId);
        Set<String> nextMissing = new LinkedHashSet<>(missingRouteChannelIds);
        if (missingRoute) {
            nextMissing.add(salesChannelId);
        }
        return copy(
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                nextChecked,
                nextMissing,
                writtenPublications,
                compensatedPublicationKeys
        );
    }

    public UpdateSellableProductContext withPublicationWritten(PublicationPair publication) {
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
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                nextWritten,
                compensatedPublicationKeys
        );
    }

    public UpdateSellableProductContext withPublicationCompensated(String variantId, String salesChannelId) {
        Set<String> next = new LinkedHashSet<>(compensatedPublicationKeys);
        next.add(publicationKey(variantId, salesChannelId));
        return copy(
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                next
        );
    }

    public UpdateSellableProductContext withUnpublished(PublicationPair unpublished) {
        String key = publicationKey(unpublished.variantId(), unpublished.salesChannelId());
        if (unpublishedPublications.stream().anyMatch(existing -> publicationKey(
                existing.variantId(),
                existing.salesChannelId()
        ).equals(key))) {
            return this;
        }
        List<PublicationPair> nextUnpublished = new ArrayList<>(unpublishedPublications);
        nextUnpublished.add(unpublished);
        return copy(
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                unpublishLines,
                nextUnpublished,
                compensatedUnpublishKeys
        );
    }

    public UpdateSellableProductContext withUnpublishCompensated(String variantId, String salesChannelId) {
        Set<String> next = new LinkedHashSet<>(compensatedUnpublishKeys);
        next.add(publicationKey(variantId, salesChannelId));
        return copy(
                productId,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                unpublishLines,
                unpublishedPublications,
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
    public boolean shouldPublish() {
        return !publicationLines.isEmpty();
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

    @JsonIgnore
    public boolean shouldUnpublish() {
        return !unpublishLines.isEmpty();
    }

    @JsonIgnore
    public boolean allUnpublished() {
        return unpublishedPublications.size() >= unpublishLines.size();
    }

    @JsonIgnore
    public boolean allUnpublishedCompensated() {
        return unpublishedPublications.stream()
                .map(pair -> publicationKey(pair.variantId(), pair.salesChannelId()))
                .allMatch(compensatedUnpublishKeys::contains);
    }

    @JsonIgnore
    public boolean isPartiallyApplied() {
        return productUpdated
                || !pricePairs.isEmpty()
                || !inventoryItemIds.isEmpty()
                || (!writtenPublications.isEmpty() && !allWrittenPublicationsCompensated())
                || (!unpublishedPublications.isEmpty() && !allUnpublishedCompensated());
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
        return variantIdForSku(pricingLine.sku());
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

    private List<PricingLine> assignPricingVariantIds(List<VariantRef> refs) {
        if (refs == null || refs.isEmpty()) {
            return pricingLines;
        }
        return pricingLines.stream()
                .map(line -> assignPricingVariantId(line, refs))
                .toList();
    }

    private PricingLine assignPricingVariantId(PricingLine line, List<VariantRef> refs) {
        VariantRef match = matchingRef(line.sku(), refs);
        if (match == null) {
            return line;
        }
        return line.withVariantId(match.variantId());
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

    private List<PublicationLine> assignResolvableUnpublishLines(
            List<PublicationLine> lines,
            List<VariantRef> refs
    ) {
        if (refs == null || refs.isEmpty()) {
            return List.of();
        }
        return lines.stream()
                .filter(line -> matchingRef(line.sku(), refs) != null)
                .map(line -> assignPublicationVariantId(line, refs))
                .toList();
    }

    private PublicationLine assignPublicationVariantId(PublicationLine line, List<VariantRef> refs) {
        VariantRef match = matchingRef(line.sku(), refs);
        if (match == null) {
            return line;
        }
        return line.withVariantId(match.variantId());
    }

    private VariantRef matchingRef(String sku, List<VariantRef> refs) {
        return refs.stream()
                .filter(ref -> ref.sku().equals(sku))
                .findFirst()
                .orElse(null);
    }

    private UpdateSellableProductContext copy(
            String newProductId,
            List<PricingLine> newPricingLines,
            List<PublicationLine> newPublicationLines,
            List<VariantRef> newVariantRefs,
            List<PricePair> newPricePairs,
            List<String> newCreatedPriceSetIds,
            List<String> newInventoryItemIds,
            List<String> newCreatedInventoryItemIds,
            boolean newProductUpdated,
            Set<String> newCompensatedPriceSetIds,
            Set<String> newAssertedChannelIds,
            boolean newProductAsserted,
            Set<String> newStockPathCheckedChannelIds,
            Set<String> newMissingRouteChannelIds,
            List<PublicationPair> newWrittenPublications,
            Set<String> newCompensatedPublicationKeys
    ) {
        return copy(
                newProductId,
                newPricingLines,
                newPublicationLines,
                newVariantRefs,
                newPricePairs,
                newCreatedPriceSetIds,
                newInventoryItemIds,
                newCreatedInventoryItemIds,
                newProductUpdated,
                newCompensatedPriceSetIds,
                newAssertedChannelIds,
                newProductAsserted,
                newStockPathCheckedChannelIds,
                newMissingRouteChannelIds,
                newWrittenPublications,
                newCompensatedPublicationKeys,
                unpublishLines,
                unpublishedPublications,
                compensatedUnpublishKeys
        );
    }

    private UpdateSellableProductContext copy(
            String newProductId,
            List<PricingLine> newPricingLines,
            List<PublicationLine> newPublicationLines,
            List<VariantRef> newVariantRefs,
            List<PricePair> newPricePairs,
            List<String> newCreatedPriceSetIds,
            List<String> newInventoryItemIds,
            List<String> newCreatedInventoryItemIds,
            boolean newProductUpdated,
            Set<String> newCompensatedPriceSetIds,
            Set<String> newAssertedChannelIds,
            boolean newProductAsserted,
            Set<String> newStockPathCheckedChannelIds,
            Set<String> newMissingRouteChannelIds,
            List<PublicationPair> newWrittenPublications,
            Set<String> newCompensatedPublicationKeys,
            List<PublicationLine> newUnpublishLines,
            List<PublicationPair> newUnpublishedPublications,
            Set<String> newCompensatedUnpublishKeys
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
                newPublicationLines,
                newVariantRefs,
                newPricePairs,
                newCreatedPriceSetIds,
                newInventoryItemIds,
                newCreatedInventoryItemIds,
                newProductUpdated,
                newCompensatedPriceSetIds,
                newAssertedChannelIds,
                newProductAsserted,
                newStockPathCheckedChannelIds,
                newMissingRouteChannelIds,
                newWrittenPublications,
                newCompensatedPublicationKeys,
                newUnpublishLines,
                newUnpublishedPublications,
                newCompensatedUnpublishKeys,
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

    public UpdateSellableProductContext withMediasReplaced() {
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                unpublishLines,
                unpublishedPublications,
                compensatedUnpublishKeys,
                medias,
                descriptions,
                true,
                descriptionsReplaced,
                statusApplied
        );
    }

    public UpdateSellableProductContext withDescriptionsReplaced() {
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                unpublishLines,
                unpublishedPublications,
                compensatedUnpublishKeys,
                medias,
                descriptions,
                mediasReplaced,
                true,
                statusApplied
        );
    }

    public UpdateSellableProductContext withStatusApplied() {
        return new UpdateSellableProductContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                product,
                inventoryLines,
                pricingLines,
                publicationLines,
                variantRefs,
                pricePairs,
                createdPriceSetIds,
                inventoryItemIds,
                createdInventoryItemIds,
                productUpdated,
                compensatedPriceSetIds,
                assertedChannelIds,
                productAsserted,
                stockPathCheckedChannelIds,
                missingRouteChannelIds,
                writtenPublications,
                compensatedPublicationKeys,
                unpublishLines,
                unpublishedPublications,
                compensatedUnpublishKeys,
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
            VariantSync variantSync
    ) {
        public Product(String name, String categoryId, String condition, String slug, VariantSync variantSync) {
            this(name, categoryId, condition, slug, null, variantSync);
        }
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
