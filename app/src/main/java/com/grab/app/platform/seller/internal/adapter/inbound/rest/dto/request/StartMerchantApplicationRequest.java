package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StartMerchantApplicationRequest(
        @NotBlank(message = "Business type is required")
        String businessType,
        @NotBlank(message = "Display name is required")
        String displayName,
        @NotBlank(message = "Primary market country code is required")
        String primaryMarketCountryCode
) {}
