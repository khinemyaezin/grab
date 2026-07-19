package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MarkDamagedRequest(
        @Min(1) int quantity,
        String notes
) {
}
