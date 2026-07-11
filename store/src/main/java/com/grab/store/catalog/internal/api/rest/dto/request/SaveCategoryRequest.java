package com.grab.store.catalog.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record SaveCategoryRequest(
        @NotBlank String name,
        String parentId,
        Boolean active,
        Boolean listingAllowed,
        Boolean c2cAllowed
) implements Serializable {
}
