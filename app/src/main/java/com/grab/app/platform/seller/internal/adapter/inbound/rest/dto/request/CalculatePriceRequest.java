package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record CalculatePriceRequest(
        @NotEmpty(message = "PriceSet IDs cannot be empty")
        List<String> priceSetIds,
        @Valid
        PricingContextRequest context,
        String currencyCode,
        Integer quantity,
        Map<String, String> attributes
) {
    public record PricingContextRequest(
            String currencyCode,
            Integer quantity,
            Map<String, String> attributes
    ) {}

    public String resolveCurrencyCode() {
        if (context != null && context.currencyCode() != null && !context.currencyCode().isBlank()) {
            return context.currencyCode();
        }
        return currencyCode;
    }

    public Integer resolveQuantity() {
        if (context != null && context.quantity() != null) {
            return context.quantity();
        }
        return quantity;
    }

    public Map<String, String> resolveAttributes() {
        if (context != null && context.attributes() != null) {
            return context.attributes();
        }
        return attributes;
    }
}
