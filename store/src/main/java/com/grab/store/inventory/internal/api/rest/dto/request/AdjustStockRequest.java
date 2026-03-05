package com.grab.store.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.AdjustmentReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @Min(0) int newOnHandQuantity,
        @NotNull AdjustmentReason reason
) {
}
