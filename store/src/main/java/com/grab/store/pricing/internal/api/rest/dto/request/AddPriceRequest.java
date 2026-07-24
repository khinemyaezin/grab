package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record AddPriceRequest(
        String title,
        @NotBlank String currencyCode,
        @NotNull BigDecimal amount,
        Integer minQuantity,
        Integer maxQuantity,
        @Valid List<PriceRuleRequest> rules
) {
}
