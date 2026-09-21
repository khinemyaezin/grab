package com.cart.application.port.outbound;

import java.math.BigDecimal;
import java.util.Optional;

public interface PricingQuotePort {
    Optional<QuotedUnitPrice> quote(String variantId, String currencyCode, int quantity, String salesChannelId);

    record QuotedUnitPrice(BigDecimal amount, String currencyCode) {
    }
}
