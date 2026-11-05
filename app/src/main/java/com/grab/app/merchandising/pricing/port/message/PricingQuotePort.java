package com.grab.app.merchandising.pricing.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Java Port Interface for Pricing Quotes across Checkout and Storefront.
 * Replaces legacy PricingQuoteQuery.
 */
public interface PricingQuotePort {

    Optional<QuotedPriceResult> quote(
            Id variantId,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    );

    default Optional<QuotedPriceResult> quote(Id variantId, String currencyCode, int quantity, Id salesChannelId) {
        return quote(variantId, currencyCode, quantity, salesChannelId, null);
    }

    BatchQuoteResult quoteBatch(BatchQuoteRequest message);

    record QuotedPriceResult(
            BigDecimal amount,
            BigDecimal originalAmount,
            String currencyCode,
            Id priceSetId
    ) {}

    record BatchQuoteRequest(
            List<Id> variantIds,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    ) {
        public BatchQuoteRequest {
            variantIds = variantIds == null ? List.of() : List.copyOf(variantIds);
        }
    }

    record BatchQuoteResult(Map<Id, QuotedPriceResult> quotes) {
        public BatchQuoteResult {
            quotes = quotes == null ? Map.of() : Map.copyOf(quotes);
        }
    }
}

