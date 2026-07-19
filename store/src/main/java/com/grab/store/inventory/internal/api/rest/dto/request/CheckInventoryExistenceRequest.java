package com.grab.store.inventory.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CheckInventoryExistenceRequest(
        @NotBlank String locationId,
        @NotEmpty @Size(max = 100) List<@NotBlank String> skus
) {
}
