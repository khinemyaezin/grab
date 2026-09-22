package com.grab.store.pricing.internal.api.adapter;

import com.grab.store.pricing.internal.api.adapter.mapper.PricingQuoteQueryMapper;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.port.PricingQuoteQuery;
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
public class PricingQuoteQueryAdapter implements PricingQuoteQuery {

    private final QuoteVariantPriceUseCase quoteVariantPriceUseCase;
    private final ListVariantIdsForPriceSetUseCase listVariantIdsForPriceSetUseCase;
    private final PricingQuoteQueryMapper mapper;

    @Override
    @PricingReadTransactional
    public Optional<QuotedPrice> quote(String variantId, String currencyCode, int quantity, String salesChannelId) {
        return quoteVariantPriceUseCase.execute(new QuoteVariantPriceQuery(
                variantId,
                currencyCode,
                quantity,
                salesChannelId
        )).map(mapper::toQuotedPrice);
    }

    @Override
    @PricingReadTransactional
    public List<String> variantIdsForPriceSet(String priceSetId) {
        return listVariantIdsForPriceSetUseCase.execute(new ListVariantIdsForPriceSetQuery(priceSetId));
    }
}
