package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStorefrontProfileRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String slug
) {
}
