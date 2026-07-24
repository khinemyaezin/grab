package com.grab.store.pricing.internal.api.rest.dto.response;

public record PriceRuleResponse(
        String id,
        String attribute,
        String value,
        String operator,
        int priority
) {
}
