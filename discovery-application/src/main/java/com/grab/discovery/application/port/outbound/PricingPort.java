package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PricingPort {
    Optional<QuotedPrice> quote(Id variantId, String currencyCode, int quantity, Id salesChannelId);

    List<Id> variantIdsForPriceSet(Id priceSetId);

    record QuotedPrice(BigDecimal amount, BigDecimal originalAmount, String currencyCode, Id priceSetId) {
        public QuotedPrice(BigDecimal amount, String currencyCode, Id priceSetId) {
            this(amount, null, currencyCode, priceSetId);
        }
    }
}
