package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DeallocateStockRequest(
        @NotBlank String sku,
        @Min(1) int quantity,
        @NotBlank String orderId
) {
}
