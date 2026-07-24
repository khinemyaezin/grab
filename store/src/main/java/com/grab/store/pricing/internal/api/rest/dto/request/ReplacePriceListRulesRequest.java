package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReplacePriceListRulesRequest(
        @NotNull @Valid List<PriceListRuleRequest> rules
) {
}
