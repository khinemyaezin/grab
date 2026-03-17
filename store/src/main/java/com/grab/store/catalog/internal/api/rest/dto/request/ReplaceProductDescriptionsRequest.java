package com.grab.store.catalog.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record ReplaceProductDescriptionsRequest(
        @Valid @NotNull List<Description> descriptions
) implements Serializable {
    public record Description(
            String id,
            @NotBlank String name,
            String title,
            @NotBlank String description
    ) implements Serializable {}
}
