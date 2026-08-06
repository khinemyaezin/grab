package com.grab.store.workflows.events;

import com.grab.framework.domain.Event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RequestCreateVariantPriceEvent(
        String workflowId,
        String variantId,
        String sku,
        String productId,
        String merchantId,
        String title,
        String currencyCode,
        BigDecimal amount,
        Integer minQuantity,
        Integer maxQuantity,
        List<PriceRule> rules,
        Instant occurredAt,
        int version
) implements Event {

    public RequestCreateVariantPriceEvent {
        rules = rules == null ? List.of() : List.copyOf(rules);
    }

    public record PriceRule(
            String attribute,
            String value,
            String operator,
            Integer priority
    ) {
    }
}
