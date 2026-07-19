package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateReorderConfigRequest(
        @NotNull @Min(0) Integer safetyStock,
        @NotNull @Min(0) Integer reorderPoint,
        @NotNull @Min(0) Integer reorderQuantity,
        @Min(0) Integer maxStock
) {
}
