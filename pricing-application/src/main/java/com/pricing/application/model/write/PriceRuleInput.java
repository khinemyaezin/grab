package com.pricing.application.model.write;

public record PriceRuleInput(
        String attribute,
        String value,
        String operator,
        Integer priority
) {
}
