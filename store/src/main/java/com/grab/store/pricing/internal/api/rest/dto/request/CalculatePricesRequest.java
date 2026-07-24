package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record CalculatePricesRequest(
        @NotEmpty List<String> priceSetIds,
        @NotNull @Valid PricingContextRequest context
) {
    public record PricingContextRequest(
            @NotBlank String currencyCode,
            Integer quantity,
            Map<String, String> attributes
    ) {
    }
}
