package com.grab.store.workflows.internal.updatesellableproduct.rest.dto.request;

import com.grab.store.workflows.events.InventorySyncOp;
import com.inventory.domain.enums.AdjustmentReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@ValidInventoryLines
public record UpdateSellableProductRequest(
        @NotBlank String productId,
        @Valid @NotNull Product product,
        @Valid List<InventoryLine> inventoryLines,
        @Valid List<PricingLine> pricingLines,
        String idempotencyKey
) {

    public record Product(
            @NotBlank String name,
            @NotBlank String categoryId,
            String condition,
            String slug,
            @Valid VariantSync variantSync
    ) {
    }

    public record VariantSync(
            String intent,
            @Valid List<Variant> overrides,
            @Valid List<VariantType> variantTypes
    ) {
    }

    public record Variant(
            @NotBlank String sku,
            String matrixKey,
            @Valid List<Variation> variations
    ) {
    }

    public record Variation(
            @NotBlank String typeId,
            @NotBlank String optionId
    ) {
    }

    public record VariantType(
            @NotBlank String typeId,
            @Valid List<VariantOption> options
    ) {
    }

    public record VariantOption(
            @NotBlank String optionId,
            String optionName
    ) {
    }

    @ValidInventoryLine
    public record InventoryLine(
            @NotBlank String sku,
            String locationId,
            String inventoryItemId,
            @NotNull InventorySyncOp op,
            @Valid CreateStock create,
            @Valid AdjustStock adjust,
            @Valid DamageStock damage,
            @Valid WriteOffStock writeOff,
            @Valid Reorder reorder
    ) {
    }

    public record CreateStock(
            @Min(0) int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) {
    }

    public record AdjustStock(
            @Min(0) int newOnHandQuantity,
            @NotNull AdjustmentReason reason
    ) {
    }

    public record DamageStock(
            @Min(1) int quantity,
            String notes
    ) {
    }

    public record WriteOffStock(
            @Min(1) int quantity,
            @NotBlank String reason,
            String notes
    ) {
    }

    public record Reorder(
            @NotNull @Min(0) Integer safetyStock,
            @NotNull @Min(0) Integer reorderPoint,
            @NotNull @Min(0) Integer reorderQuantity,
            @Min(0) Integer maxStock
    ) {
    }

    public record PricingLine(
            @NotBlank String sku,
            String priceSetId,
            String priceId,
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
