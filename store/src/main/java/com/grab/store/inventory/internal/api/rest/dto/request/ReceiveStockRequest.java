package com.grab.store.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceiveStockRequest(
        @Min(1) int quantity,
        @NotNull StockMovementType type,
        String referenceId
) {
}
