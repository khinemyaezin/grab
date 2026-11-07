package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StartMerchantApplicationRequest(
        String businessType,
        @NotBlank(message = "Display name is required")
        @Size(max = 255)
        String displayName,
        @NotBlank(message = "Primary market country code is required")
        @Size(min = 2, max = 2)
        @Pattern(regexp = "[A-Za-z]{2}", message = "Country code must be a 2-letter ISO code")
        String primaryMarketCountryCode
) {}
