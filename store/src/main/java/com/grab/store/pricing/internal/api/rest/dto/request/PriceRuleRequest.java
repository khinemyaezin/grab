package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PriceRuleRequest(
        @NotBlank String attribute,
        @NotBlank String value,
        String operator,
        Integer priority
) {
}
