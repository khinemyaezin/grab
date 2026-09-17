package com.grab.store.merchant.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStorefrontRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String slug
) {
}
