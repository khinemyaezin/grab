package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.query.CalculatePricesQuery;
import com.grab.store.pricing.internal.query.CalculatedPriceSetResult;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.policy.CalculatePricesPolicy;
import com.pricing.domain.policy.CalculatedPriceSet;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.policy.PricePreferenceView;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.PricingContext;
import com.pricing.infrastructure.repository.jpa.PriceQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class CalculatePricesQueryHandler
        implements QueryHandler<CalculatePricesQuery, List<CalculatedPriceSetResult>> {

    private final PriceQueryRepository priceQueryRepository;
    private final CalculatePricesPolicy calculatePricesPolicy = new CalculatePricesPolicy();

    @Override
    @PricingReadTransactional
    public List<CalculatedPriceSetResult> handle(CalculatePricesQuery query) {
        if (query.currencyCode() == null || query.currencyCode().isBlank()) {
            throw new PricingServiceException(
                    new PricingServiceError.CurrencyRequired(),
                    "currencyCode is required"
            );
        }
        CurrencyCode currencyCode = CurrencyCode.of(query.currencyCode());
        Map<String, String> attributes = query.attributes() == null ? Map.of() : query.attributes();
        PricingContext context = new PricingContext(currencyCode, query.quantity(), attributes);
        List<PriceCandidate> candidates = priceQueryRepository.findCandidates(
                query.priceSetIds(),
                currencyCode.value()
        );
        List<PricePreferenceView> preferences = priceQueryRepository.findPreferences();
        List<CalculatedPriceSet> calculated = calculatePricesPolicy.calculate(
                query.priceSetIds(),
                context,
                candidates,
                preferences,
                Instant.now()
        );
        return calculated.stream().map(PricingResultMapper::toCalculatedResult).toList();
    }

    @Override
    public Class<CalculatePricesQuery> getQueryType() {
        return CalculatePricesQuery.class;
    }
}
