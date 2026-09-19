package com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@ValidInventoryTracking
@ValidPricingCoverage
public record CreateSellableProductRequest(
        @Valid @NotNull Product product,
        @Valid List<VariantType> variantTypes,
        @Valid List<InventoryLine> inventoryLines,
        @Valid @NotEmpty List<PricingLine> pricingLines,
        @Valid List<PublicationLine> publicationLines,
        @Valid List<Media> medias,
        @Valid List<Description> descriptions,
        String idempotencyKey
) {

    public record Product(
            @NotBlank String name,
            @NotBlank String categoryId,
            String condition,
            String slug,
            String status,
            @Valid List<Variant> variants
    ) {
        public Product(String name, String categoryId, String condition, String slug, List<Variant> variants) {
            this(name, categoryId, condition, slug, null, variants);
        }
    }

    public record VariantType(
            @NotBlank String typeId,
            @Valid List<VariantOption> options
    ) {
    }

    public record VariantOption(
            @NotBlank String optionId
    ) {
    }

    public record Variant(
            @NotBlank String sku,
            @Valid List<Variation> variations,
            Boolean manageInventory
    ) {
        public Variant(String sku, List<Variation> variations) {
            this(sku, variations, null);
        }
    }

    public record Variation(
            @NotBlank String optionId,
            @NotBlank String typeId
    ) {
    }

    public record InventoryLine(
            @NotBlank String sku,
            @NotBlank String locationId,
            @Min(0) int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) {
    }

    public record PricingLine(
            @NotBlank String sku,
            String title,
            @NotBlank String currencyCode,
            @NotNull BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            @Valid List<PriceRule> rules
    ) {
    }

    public record PriceRule(
            @NotBlank String attribute,
            @NotBlank String value,
            String operator,
            Integer priority
    ) {
    }

    public record PublicationLine(
            @NotBlank String sku,
            @NotBlank String salesChannelId
    ) {
    }

    public record Media(
            String id,
            @NotBlank String storageKey,
            String contentType,
            Integer rank
    ) {
    }

    public record Description(
            String id,
            @NotBlank String name,
            String title,
            @NotBlank String description
    ) {
    }
}
