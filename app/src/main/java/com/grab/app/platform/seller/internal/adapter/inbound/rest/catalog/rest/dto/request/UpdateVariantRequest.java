package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateVariantRequest(
        @NotBlank String sku
) {}
