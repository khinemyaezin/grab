package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TransferInventoryRequest(
        @NotBlank String toLocationId,
        @Min(1) int quantity,
        String notes
) {
}
