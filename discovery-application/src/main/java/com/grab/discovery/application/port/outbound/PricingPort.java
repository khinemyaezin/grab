package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PricingPort {
    Optional<QuotedPrice> quote(Id variantId, String currencyCode, int quantity, Id salesChannelId);

    default Optional<QuotedPrice> quote(
            Id variantId,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    ) {
        return quote(variantId, currencyCode, quantity, salesChannelId);
    }

    Map<Id, QuotedPrice> quoteBatch(
            List<Id> variantIds,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    );

    List<Id> variantIdsForPriceSet(Id priceSetId);

    record QuotedPrice(BigDecimal amount, BigDecimal originalAmount, String currencyCode, Id priceSetId) {
        public QuotedPrice(BigDecimal amount, String currencyCode, Id priceSetId) {
            this(amount, null, currencyCode, priceSetId);
        }
    }
}
