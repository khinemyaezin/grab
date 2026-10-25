package com.grab.app.ordermanagement.cart.internal.adapter;

import com.cart.application.port.outbound.PricingQuotePort;
import com.grab.app.productmerchandising.pricing.port.PricingQuoteQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cartPricingQuoteAdapter")
@RequiredArgsConstructor
public class PricingQuoteAdapter implements PricingQuotePort {
    private final PricingQuoteQuery pricingQuoteQuery;

    @Override
    public Optional<QuotedUnitPrice> quote(String variantId, String currencyCode, int quantity, String salesChannelId) {
        return pricingQuoteQuery.quote(variantId, currencyCode, quantity, salesChannelId)
                .map(quoted -> new QuotedUnitPrice(quoted.amount(), quoted.currencyCode()));
    }
}
