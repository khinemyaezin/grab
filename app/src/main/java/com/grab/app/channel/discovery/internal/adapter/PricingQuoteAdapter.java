package com.grab.app.channel.discovery.internal.adapter;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.id.Id;
import com.grab.app.catalog.pricing.port.PricingQuoteQuery;
import com.grab.app.channel.discovery.internal.adapter.mapper.PricingQuoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component("storeFrontQueryPricingQuoteAdapter")
@RequiredArgsConstructor
public class PricingQuoteAdapter implements PricingPort {
    private final PricingQuoteQuery pricing;
    private final PricingQuoteMapper mapper;

    @Override
    public Optional<QuotedPrice> quote(
            Id variantId,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    ) {
        String variantValue = variantId.getValue();
        String channelValue = salesChannelId.getValue();
        String regionValue = regionId == null ? null : regionId.getValue();
        Optional<PricingQuoteQuery.QuotedPrice> providerQuote = pricing.quote(
                variantValue,
                currencyCode,
                quantity,
                channelValue,
                regionValue
        );
        return providerQuote.map(mapper::toQuotedPrice);
    }

    @Override
    public Map<Id, QuotedPrice> quoteBatch(
            List<Id> variantIds,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId
    ) {
        List<String> variantValues = toVariantValues(variantIds);
        String channelValue = salesChannelId.getValue();
        String regionValue = regionId == null ? null : regionId.getValue();
        PricingQuoteQuery.BatchQuoteRequest request = new PricingQuoteQuery.BatchQuoteRequest(
                variantValues,
                currencyCode,
                quantity,
                channelValue,
                regionValue
        );
        PricingQuoteQuery.BatchQuoteResult batch = pricing.quoteBatch(request);
        Map<Id, QuotedPrice> result = new LinkedHashMap<>();
        for (Id variantId : variantIds) {
            String variantValue = variantId.getValue();
            PricingQuoteQuery.QuotedPrice quote = batch.quotes().get(variantValue);
            if (quote == null) {
                throw new IllegalStateException("Pricing batch response is missing a requested variant.");
            }
            QuotedPrice mapped = mapper.toQuotedPrice(quote);
            result.put(variantId, mapped);
        }
        return Collections.unmodifiableMap(result);
    }

    private List<String> toVariantValues(List<Id> variantIds) {
        List<String> result = new ArrayList<>();
        for (Id variantId : variantIds) {
            result.add(variantId.getValue());
        }
        return result;
    }
}
