package com.grab.store.workflows.internal.createsellableproduct.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateSellableProductRequest(
        @Valid @NotNull Product product,
        @Valid List<VariantType> variantTypes,
        @Valid @NotEmpty List<InventoryLine> inventoryLines,
        @Valid @NotEmpty List<PricingLine> pricingLines,
        String idempotencyKey
) {

    public record Product(
            @NotBlank String name,
            @NotBlank String categoryId,
            String condition,
            String slug,
            @Valid List<Variant> variants
    ) {
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
            @Valid List<Variation> variations
    ) {
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
}
