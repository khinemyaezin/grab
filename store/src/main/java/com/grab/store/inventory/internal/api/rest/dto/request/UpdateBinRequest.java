package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateBinRequest(
        String code,
        String name,
        @Min(1) Integer maxCapacity,
        Boolean active
) {
}
