package com.grab.store.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.LocationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLocationRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull LocationType type,
        @Valid @NotNull AddressRequest address
) {
}
