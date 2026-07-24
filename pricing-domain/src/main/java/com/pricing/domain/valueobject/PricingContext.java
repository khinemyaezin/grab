package com.pricing.domain.valueobject;

import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record PricingContext(
        CurrencyCode currencyCode,
        Integer quantity,
        Map<String, String> attributes
) {
    public PricingContext {
        if (currencyCode == null) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("currencyCode"),
                    "currencyCode is required in pricing context"
            );
        }
        attributes = attributes == null
                ? Map.of()
                : Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    public Optional<String> attribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public Optional<String> regionId() {
        return attribute(PricingAttributeKeys.REGION_ID);
    }

    public Optional<String> customerGroupId() {
        return attribute(PricingAttributeKeys.CUSTOMER_GROUP_ID);
    }

    public Optional<String> salesChannelId() {
        return attribute(PricingAttributeKeys.SALES_CHANNEL_ID);
    }

    public int effectiveQuantity() {
        return quantity == null ? 1 : quantity;
    }
}
