package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ReserveStockRequest(
        @Min(1) int quantity,
        @NotBlank String orderId,
        @NotBlank String orderLineId,
        LocalDateTime expiresAt
) {
}
