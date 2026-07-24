package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PriceListRuleRequest(
        @NotBlank String attribute,
        @NotEmpty List<String> values
) {
}
