package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateInventoryRequest(
        @NotBlank String sku,
        @NotBlank String merchantId,
        String productVariantId,
        @NotBlank String locationId,
        @Min(0) int initialQuantity,
        Integer safetyStock,
        Integer reorderPoint,
        Integer reorderQuantity,
        Integer maxStock
) {
}
