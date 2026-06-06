package com.grab.store.inventory.internal.api.rest.dto.request;

import com.inventory.domain.enums.ZoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateZoneRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull ZoneType type
) {
}
