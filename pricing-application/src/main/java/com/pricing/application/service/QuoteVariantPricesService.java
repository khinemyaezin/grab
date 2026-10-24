package com.pricing.application.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.application.port.inbound.QuoteVariantPricesUseCase;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.policy.CalculatePricesPolicy;
import com.pricing.domain.policy.CalculatedPriceSet;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.policy.PricePreferenceView;
import com.pricing.domain.policy.QuoteVariantPricesPolicy;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import com.pricing.domain.valueobject.PricingContext;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class QuoteVariantPricesService implements QuoteVariantPricesUseCase {

    private final VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort;
    private final PriceQueryPort priceQueryPort;
    private final CalculatePricesPolicy calculatePricesPolicy;
    private final IdGenerator idGenerator;
    private final QuoteVariantPricesPolicy quoteVariantPricesPolicy;

    public QuoteVariantPricesService(
            VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort,
            PriceQueryPort priceQueryPort,
            CalculatePricesPolicy calculatePricesPolicy,
            IdGenerator idGenerator
    ) {
        this(
                variantPriceSetLinkQueryPort,
                priceQueryPort,
                calculatePricesPolicy,
                idGenerator,
                new QuoteVariantPricesPolicy()
        );
    }

    @Override
    public QuoteVariantPricesResult execute(QuoteVariantPricesQuery query) {
        validate(query);

        LinkedHashMap<String, Id> requestedVariants = deduplicate(query.variantIds());
        if (requestedVariants.isEmpty()) {
            return new QuoteVariantPricesResult(Map.of());
        }

        List<String> requestedVariantValues = new ArrayList<>(requestedVariants.keySet());
        List<VariantPriceSetLinkView> links = variantPriceSetLinkQueryPort.findByVariantIds(requestedVariantValues);
        Map<String, VariantPriceSetLinkView> linksByVariant = indexLinks(links);
        Set<String> priceSetValues = collectPriceSetValues(requestedVariants, linksByVariant);
        if (priceSetValues.isEmpty()) {
            Map<String, CalculatedPriceSetResult> noCalculatedPrices = Map.of();
            return unpricedResults(requestedVariants, linksByVariant, noCalculatedPrices);
        }

        CurrencyCode currencyCode = CurrencyCode.of(query.currencyCode());
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(PricingAttributeKeys.SALES_CHANNEL_ID, query.salesChannelId().getValue());
        attributes.put(PricingAttributeKeys.REGION_ID, query.regionId().getValue());
        List<Id> priceSetIds = toIds(priceSetValues);
        PricingContext context = new PricingContext(currencyCode, query.quantity(), attributes);

        List<PriceCandidate> candidates = priceQueryPort.findCandidates(priceSetIds, currencyCode.value());
        List<PricePreferenceView> preferences = priceQueryPort.findPreferences();
        List<CalculatedPriceSet> calculated = calculatePricesPolicy.calculate(
                priceSetIds,
                context,
                candidates,
                preferences,
                Instant.now()
        );

        List<CalculatedPriceSetResult> calculatedResults = calculated.stream()
                .map(PricingResultMapper::toCalculatedResult)
                .toList();
        Map<String, CalculatedPriceSetResult> calculatedByPriceSet = indexCalculated(calculatedResults);
        return unpricedResults(requestedVariants, linksByVariant, calculatedByPriceSet);
    }

    private void validate(QuoteVariantPricesQuery query) {
        if (query == null) {
            throw new PricingServiceException(
                    new PricingServiceError.InvalidBatchQuote("query"),
                    "query is required"
            );
        }
        try {
            quoteVariantPricesPolicy.validate(
                    query.variantIds(),
                    query.currencyCode(),
                    query.salesChannelId(),
                    query.regionId(),
                    query.quantity()
            );
        } catch (IllegalArgumentException exception) {
            String field = exception.getMessage();
            throw new PricingServiceException(
                    new PricingServiceError.InvalidBatchQuote(field),
                    "Invalid batch quote field: " + field
            );
        }
    }

    private LinkedHashMap<String, Id> deduplicate(List<Id> variantIds) {
        LinkedHashMap<String, Id> result = new LinkedHashMap<>();
        for (Id variantId : variantIds) {
            result.putIfAbsent(variantId.getValue(), variantId);
        }
        return result;
    }

    private Map<String, VariantPriceSetLinkView> indexLinks(List<VariantPriceSetLinkView> links) {
        Map<String, VariantPriceSetLinkView> result = new LinkedHashMap<>();
        for (VariantPriceSetLinkView link : links) {
            if (link != null && link.variantId() != null) {
                result.putIfAbsent(link.variantId(), link);
            }
        }
        return result;
    }

    private Set<String> collectPriceSetValues(
            LinkedHashMap<String, Id> requestedVariants,
            Map<String, VariantPriceSetLinkView> linksByVariant
    ) {
        Set<String> result = new LinkedHashSet<>();
        for (String variantValue : requestedVariants.keySet()) {
            VariantPriceSetLinkView link = linksByVariant.get(variantValue);
            if (link != null && link.priceSetId() != null && !link.priceSetId().isBlank()) {
                result.add(link.priceSetId());
            }
        }
        return result;
    }

    private Map<String, CalculatedPriceSetResult> indexCalculated(List<CalculatedPriceSetResult> calculated) {
        Map<String, CalculatedPriceSetResult> result = new LinkedHashMap<>();
        for (CalculatedPriceSetResult price : calculated) {
            if (price != null && price.id() != null) {
                result.put(price.id(), price);
            }
        }
        return result;
    }

    private List<Id> toIds(Set<String> values) {
        List<Id> result = new ArrayList<>();
        for (String value : values) {
            result.add(idGenerator.convertIdFrom(value));
        }
        return result;
    }

    private QuoteVariantPricesResult unpricedResults(
            LinkedHashMap<String, Id> requestedVariants,
            Map<String, VariantPriceSetLinkView> linksByVariant,
            Map<String, CalculatedPriceSetResult> calculatedByPriceSet
    ) {
        Map<Id, QuoteVariantPricesResult.VariantQuote> result = new LinkedHashMap<>();
        for (Map.Entry<String, Id> requested : requestedVariants.entrySet()) {
            VariantPriceSetLinkView link = linksByVariant.get(requested.getKey());
            Id priceSetId = toId(link == null ? null : link.priceSetId());
            CalculatedPriceSetResult calculated = priceSetId == null
                    ? null
                    : calculatedByPriceSet.get(priceSetId.getValue());
            boolean priced = calculated != null && calculated.calculatedAmount() != null;
            QuoteVariantPricesResult.VariantQuote quote = toQuote(calculated, priceSetId, priced);
            result.put(requested.getValue(), quote);
        }
        return new QuoteVariantPricesResult(result);
    }

    private QuoteVariantPricesResult.VariantQuote toQuote(
            CalculatedPriceSetResult calculated,
            Id priceSetId,
            boolean priced
    ) {
        if (priced) {
            return new QuoteVariantPricesResult.VariantQuote(
                    calculated.calculatedAmount(),
                    calculated.originalAmount(),
                    calculated.currencyCode(),
                    priceSetId
            );
        }
        String currencyCode = calculated == null ? null : calculated.currencyCode();
        return new QuoteVariantPricesResult.VariantQuote(null, null, currencyCode, priceSetId);
    }

    private Id toId(String value) {
        return value == null || value.isBlank() ? null : idGenerator.convertIdFrom(value);
    }
}
