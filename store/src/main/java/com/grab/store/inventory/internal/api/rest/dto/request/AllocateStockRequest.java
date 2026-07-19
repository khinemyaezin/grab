package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record AllocateStockRequest(
        @NotBlank String sku,
        @Min(1) int quantity,
        @NotBlank String orderId,
        String orderLineId,
        String locationId,
        LocalDateTime expiresAt
) {
}
