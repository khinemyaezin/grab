package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AnnounceInTransitRequest(
        @NotNull @Min(1) Integer quantity,
        String referenceId
) {
}
