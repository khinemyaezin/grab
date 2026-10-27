package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request;

public record UpdateBinRequest(
        String code,
        String name,
        Integer maxCapacity,
        Boolean active
) {
}
