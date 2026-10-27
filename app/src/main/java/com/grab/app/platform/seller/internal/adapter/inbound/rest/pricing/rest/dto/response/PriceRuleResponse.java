package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response;

public record PriceRuleResponse(
        String id,
        String attribute,
        String value,
        String operator,
        int priority
) {
}
