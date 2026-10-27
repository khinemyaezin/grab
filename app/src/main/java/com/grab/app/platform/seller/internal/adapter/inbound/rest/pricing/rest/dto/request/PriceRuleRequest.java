package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PriceRuleRequest(
        @NotBlank String attribute,
        @NotBlank String value,
        String operator,
        Integer priority
) {
}
