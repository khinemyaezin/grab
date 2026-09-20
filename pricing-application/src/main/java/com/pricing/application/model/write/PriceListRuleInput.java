package com.pricing.application.model.write;

import java.util.List;

public record PriceListRuleInput(String attribute, List<String> values) {
}
