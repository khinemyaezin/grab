package com.grab.store.pricing.internal.api.query;

import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.query.PricingQuoteQueryPort;
import com.pricing.application.model.read.ListVariantIdsForPriceSetQuery;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.port.inbound.ListVariantIdsForPriceSetUseCase;
import com.pricing.application.port.inbound.QuoteVariantPriceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PricingQuoteQueryPortAdapter implements PricingQuoteQueryPort {

    private final QuoteVariantPriceUseCase quoteVariantPriceUseCase;
    private final ListVariantIdsForPriceSetUseCase listVariantIdsForPriceSetUseCase;

    @Override
    @PricingReadTransactional
    public Optional<QuotedPrice> quote(String variantId, String currencyCode, int quantity, String salesChannelId) {
        return quoteVariantPriceUseCase.execute(new QuoteVariantPriceQuery(
                variantId,
                currencyCode,
                quantity,
                salesChannelId
        )).map(quoted -> new QuotedPrice(quoted.amount(), quoted.currencyCode(), quoted.priceSetId()));
    }

    @Override
    @PricingReadTransactional
    public List<String> variantIdsForPriceSet(String priceSetId) {
        return listVariantIdsForPriceSetUseCase.execute(new ListVariantIdsForPriceSetQuery(priceSetId));
    }
}
