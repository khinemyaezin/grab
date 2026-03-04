package com.grab.store.catalog.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProductRequest(
        @NotBlank String name,
        @NotBlank String categoryId,
        String slug,
        Boolean featured
) {}
