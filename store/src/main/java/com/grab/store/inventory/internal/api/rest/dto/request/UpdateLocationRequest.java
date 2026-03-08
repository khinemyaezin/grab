package com.grab.store.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.LocationType;
import jakarta.validation.Valid;

public record UpdateLocationRequest(
        String code,
        String name,
        LocationType type,
        @Valid AddressRequest address
) {
}
