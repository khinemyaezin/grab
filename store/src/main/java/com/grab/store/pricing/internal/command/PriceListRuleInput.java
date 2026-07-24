package com.grab.store.pricing.internal.command;

import java.util.List;

public record PriceListRuleInput(String attribute, List<String> values) {
}
