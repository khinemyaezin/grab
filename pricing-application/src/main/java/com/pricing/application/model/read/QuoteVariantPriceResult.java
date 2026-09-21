package com.pricing.application.model.read;

import java.math.BigDecimal;

public record QuoteVariantPriceResult(BigDecimal amount, String currencyCode, String priceSetId) {
}
