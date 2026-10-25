package com.grab.app.productmerchandising.inventory.internal.api.rest.dto.request;

public record UpdateBinRequest(
        String code,
        String name,
        Integer maxCapacity,
        Boolean active
) {
}
