package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.port.PricingQuoteQuery;
import com.grab.store.storefrontquery.internal.adapter.mapper.PricingQuoteMapper;
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
    private final IdGenerator idGenerator;
    private final PricingQuoteMapper mapper;

    @Override
    public Optional<QuotedPrice> quote(Id variantId, String currencyCode, int quantity, Id salesChannelId) {
        return quote(variantId, currencyCode, quantity, salesChannelId, null);
    }

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
            QuotedPrice mapped = mapper.toQuotedPrice(quote);
            QuotedPrice outcome = mapped == null ? new QuotedPrice(null, null, null, null) : mapped;
            result.put(variantId, outcome);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public List<Id> variantIdsForPriceSet(Id priceSetId) {
        String priceSetValue = priceSetId.getValue();
        List<String> variantValues = pricing.variantIdsForPriceSet(priceSetValue);
        return toIds(variantValues);
    }

    private List<String> toVariantValues(List<Id> variantIds) {
        List<String> result = new ArrayList<>();
        for (Id variantId : variantIds) {
            result.add(variantId.getValue());
        }
        return result;
    }

    private List<Id> toIds(List<String> variantValues) {
        List<Id> result = new ArrayList<>();
        for (String variantValue : variantValues) {
            Id variantId = idGenerator.convertIdFrom(variantValue);
            result.add(variantId);
        }
        return result;
    }

}
