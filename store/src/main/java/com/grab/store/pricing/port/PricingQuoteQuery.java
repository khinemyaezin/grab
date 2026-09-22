package com.grab.store.pricing.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PricingQuoteQuery {
    Optional<QuotedPrice> quote(String variantId, String currencyCode, int quantity, String salesChannelId);

    List<String> variantIdsForPriceSet(String priceSetId);

    record QuotedPrice(BigDecimal amount, String currencyCode, String priceSetId) {
    }
}
