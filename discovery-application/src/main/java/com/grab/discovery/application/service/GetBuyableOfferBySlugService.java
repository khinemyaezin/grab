package com.grab.discovery.application.service;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.exception.StorefrontQueryServiceError;
import com.grab.discovery.application.exception.StorefrontQueryServiceException;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.discovery.application.policy.ContextualOfferPolicy;
import com.grab.framework.id.Id;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class GetBuyableOfferBySlugService implements GetBuyableOfferBySlugUseCase {
    private final DiscoveryProductQueryPort discoveryProductQueryPort;
    private final PricingPort pricingPort;
    private final ContextualOfferPolicy contextualOfferPolicy;

    @Override
    public BuyableOfferResult execute(GetBuyableOfferBySlugQuery query) {
        DiscoveryProductView product = discoveryProductQueryPort.findVisibleBySlug(
                        query.context().salesChannelId(), query.slug())
                .orElseThrow(() -> new StorefrontQueryServiceException(
                        new StorefrontQueryServiceError.OfferNotFound(
                                query.slug(),
                                query.context().salesChannelId().getValue()
                        ),
                        "Offer not found."
                ));
        Set<Id> variantIds = collectVariantIds(product);
        Map<Id, ContextualOfferPolicy.Quote> quotes = quote(variantIds, query);
        return contextualOfferPolicy.compose(product, query.context(), query.regionServed(), quotes);
    }

    private Set<Id> collectVariantIds(DiscoveryProductView product) {
        Set<Id> variantIds = new LinkedHashSet<>();
        product.variants().stream()
                .filter(variant -> "ACTIVE".equals(variant.status()))
                .map(DiscoveryProductView.VariantFact::variantId)
                .forEach(variantIds::add);
        return variantIds;
    }

    private Map<Id, ContextualOfferPolicy.Quote> quote(Set<Id> variantIds, GetBuyableOfferBySlugQuery query) {
        if (!query.regionServed() || variantIds.isEmpty()) {
            return Map.of();
        }
        Map<Id, PricingPort.QuotedPrice> providerQuotes = pricingPort.quoteBatch(
                List.copyOf(variantIds),
                query.context().currencyCode(),
                1,
                query.context().salesChannelId(),
                query.context().regionId()
        );
        Map<Id, ContextualOfferPolicy.Quote> quotes = new LinkedHashMap<>();
        if (providerQuotes != null) {
            for (Map.Entry<Id, PricingPort.QuotedPrice> entry : providerQuotes.entrySet()) {
                quotes.put(entry.getKey(), toPolicyQuote(entry.getValue()));
            }
        }
        return quotes;
    }

    private ContextualOfferPolicy.Quote toPolicyQuote(PricingPort.QuotedPrice quote) {
        return quote == null ? null : new ContextualOfferPolicy.Quote(
                quote.amount(),
                quote.originalAmount(),
                quote.currencyCode()
        );
    }
}
