package com.grab.store.pricing.internal.command;

public record PriceRuleInput(
        String attribute,
        String value,
        String operator,
        Integer priority
) {
}
