package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStorefrontRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String slug
) {
}
