package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.port.PricingQuoteQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("storeFrontQueryPricingQuoteAdapter")
@RequiredArgsConstructor
public class PricingQuoteAdapter implements PricingPort {
    private final PricingQuoteQuery pricing;
    private final IdGenerator idGenerator;

    @Override
    public Optional<QuotedPrice> quote(Id variantId, String currencyCode, int quantity, Id salesChannelId) {
        String variantValue = variantId.getValue();
        String channelValue = salesChannelId.getValue();
        Optional<PricingQuoteQuery.QuotedPrice> quoteOpt = pricing.quote(variantValue, currencyCode, quantity, channelValue);
        return quoteOpt.map(this::toQuotedPrice);
    }

    @Override
    public List<Id> variantIdsForPriceSet(Id priceSetId) {
        String priceSetValue = priceSetId.getValue();
        List<String> variantValues = pricing.variantIdsForPriceSet(priceSetValue);
        return variantValues.stream()
                .map(idGenerator::convertIdFrom)
                .toList();
    }

    private QuotedPrice toQuotedPrice(PricingQuoteQuery.QuotedPrice quote) {
        Id priceSetId = id(quote.priceSetId());
        return new QuotedPrice(quote.amount(), quote.originalAmount(), quote.currencyCode(), priceSetId);
    }

    private Id id(String value) {
        return value == null ? null : idGenerator.convertIdFrom(value);
    }
}
