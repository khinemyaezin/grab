package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.request;

import com.inventory.domain.enums.AdjustmentReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductVariantRequest(
        @NotBlank String productId,
        @NotBlank String variantId,
        @NotBlank String sku,
        @Valid Price price,
        @Valid AdjustStock adjustStock,
        String idempotencyKey
) {

    public record Price(
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

    public record AdjustStock(
            @NotBlank String inventoryItemId,
            @Min(0) int newOnHandQuantity,
            @NotNull AdjustmentReason reason
    ) {
    }
}
