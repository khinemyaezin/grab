package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBinRequest(
        String zoneId,
        @NotBlank String code,
        String name,
        Integer maxCapacity
) {
}
