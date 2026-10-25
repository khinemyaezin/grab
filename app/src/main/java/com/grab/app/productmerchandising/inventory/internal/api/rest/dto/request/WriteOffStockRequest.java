package com.grab.app.productmerchandising.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WriteOffStockRequest(
        @Min(1) int quantity,
        @NotBlank String reason,
        String notes
) {
}
