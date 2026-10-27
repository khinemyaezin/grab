package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReplacePriceListRulesRequest(
        @NotNull @Valid List<PriceListRuleRequest> rules
) {
}
