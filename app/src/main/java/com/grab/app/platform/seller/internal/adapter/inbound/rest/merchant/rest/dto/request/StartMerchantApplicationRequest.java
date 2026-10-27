package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record StartMerchantApplicationRequest(
        @NotBlank @Size(max = 255) String displayName,
        @NotBlank @Size(min = 2, max = 2) @Pattern(regexp = "[A-Za-z]{2}") String primaryMarketCountryCode
) {
}
