package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReceiveInTransitRequest(
        @NotNull @Min(1) Integer quantity,
        String referenceId
) {
}
