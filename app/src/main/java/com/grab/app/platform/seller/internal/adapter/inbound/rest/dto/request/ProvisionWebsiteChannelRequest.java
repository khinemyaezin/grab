package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProvisionWebsiteChannelRequest(
        @NotBlank(message = "Country code is required")
        String countryCode
) {}

