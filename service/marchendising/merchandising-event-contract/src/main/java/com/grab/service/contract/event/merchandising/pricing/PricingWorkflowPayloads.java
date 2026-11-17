package com.grab.service.contract.event.merchandising.pricing;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;

public final class PricingWorkflowPayloads {

    private PricingWorkflowPayloads() {
    }

    public record PriceBatch(Id merchantId, Id productId, List<Price> prices, List<Variant> variants) {
        public PriceBatch {
            prices = List.copyOf(prices);
            variants = List.copyOf(variants);
        }
    }

    public record PricesWritten(List<Id> priceSetIds) {
        public PricesWritten {
            priceSetIds = List.copyOf(priceSetIds);
        }
    }

    public record Variant(Id variantId, String sku) {
    }

    public record Price(
            Id variantId,
            String sku,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            List<Rule> rules
    ) {
        public Price {
            rules = List.copyOf(rules);
        }
    }

    public record Rule(String attribute, String value, String operator, Integer priority) {
    }
}
