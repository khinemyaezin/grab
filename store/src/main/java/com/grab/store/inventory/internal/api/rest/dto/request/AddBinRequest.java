package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddBinRequest(
        @NotBlank String code,
        String name,
        @Min(1) Integer maxCapacity,
        Boolean active
) {
}
