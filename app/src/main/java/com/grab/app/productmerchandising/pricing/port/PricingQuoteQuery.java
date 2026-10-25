package com.grab.app.productmerchandising.pricing.port;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    BatchQuoteResult quoteBatch(BatchQuoteRequest request);

    record QuotedPrice(
            BigDecimal amount,
            BigDecimal originalAmount,
            String currencyCode,
            String priceSetId
    ) {
    }

    record BatchQuoteRequest(
            List<String> variantIds,
            String currencyCode,
            int quantity,
            String salesChannelId,
            String regionId
    ) {
        public BatchQuoteRequest {
            variantIds = variantIds == null
                    ? null
                    : Collections.unmodifiableList(new ArrayList<>(variantIds));
        }
    }

    record BatchQuoteResult(Map<String, QuotedPrice> quotes) {
        public BatchQuoteResult {
            Map<String, QuotedPrice> copied = quotes == null ? Map.of() : new LinkedHashMap<>(quotes);
            quotes = Collections.unmodifiableMap(copied);
        }
    }
}
