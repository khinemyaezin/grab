package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateStorefrontRequest(
        @NotBlank(message = "Storefront name is required")
        String name,
        @NotBlank(message = "Slug is required")
        String slug
) {}
