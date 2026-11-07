package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SaveCategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,
        String parentId,
        Boolean active,
        Boolean listingAllowed,
        Boolean c2cAllowed
) {}
