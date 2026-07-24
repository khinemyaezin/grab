package com.grab.store.pricing.internal.api.rest.dto.response;

import java.util.List;

public record PriceListRuleResponse(
        String id,
        String attribute,
        List<String> values
) {
}
