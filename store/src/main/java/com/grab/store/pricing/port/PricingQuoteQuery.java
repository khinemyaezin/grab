package com.grab.store.pricing.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PricingQuoteQuery {
    Optional<QuotedPrice> quote(
            String variantId,
            String currencyCode,
            int quantity,
            String salesChannelId,
            String regionId
    );

    default Optional<QuotedPrice> quote(String variantId, String currencyCode, int quantity, String salesChannelId) {
        return quote(variantId, currencyCode, quantity, salesChannelId, null);
    }

    List<String> variantIdsForPriceSet(String priceSetId);

    record QuotedPrice(
            BigDecimal amount,
            BigDecimal originalAmount,
            String currencyCode,
            String priceSetId
    ) {
    }
}
