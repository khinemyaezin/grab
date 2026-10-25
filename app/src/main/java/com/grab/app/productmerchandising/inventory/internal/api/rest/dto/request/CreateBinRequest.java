package com.grab.app.productmerchandising.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBinRequest(
        String zoneId,
        @NotBlank String code,
        String name,
        Integer maxCapacity
) {
}
