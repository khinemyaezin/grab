package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateVariantRequest(
        @NotBlank String sku
) {}
