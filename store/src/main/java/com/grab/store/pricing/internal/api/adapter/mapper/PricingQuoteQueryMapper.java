package com.grab.store.pricing.internal.api.adapter.mapper;

import com.grab.store.pricing.port.PricingQuoteQuery.QuotedPrice;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import org.springframework.stereotype.Component;

@Component
public class PricingQuoteQueryMapper {

    public QuotedPrice toQuotedPrice(QuoteVariantPriceResult quoted) {
        return new QuotedPrice(quoted.amount(), quoted.currencyCode(), quoted.priceSetId());
    }
}
