package com.pricing.application.service;

import com.pricing.application.port.inbound.CalculatePricesUseCase;

import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.policy.CalculatePricesPolicy;
import com.pricing.domain.policy.CalculatedPriceSet;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.policy.PricePreferenceView;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.PricingContext;
import com.pricing.application.port.outbound.PriceQueryPort;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CalculatePricesService implements CalculatePricesUseCase {

    private final PriceQueryPort priceQueryPort;
    private final CalculatePricesPolicy calculatePricesPolicy;
    public List<CalculatedPriceSetResult> execute(CalculatePricesQuery query) {
        if (query.currencyCode() == null || query.currencyCode().isBlank()) {
            throw new PricingServiceException(
                    new PricingServiceError.CurrencyRequired(),
                    "currencyCode is required"
            );
        }
        CurrencyCode currencyCode = CurrencyCode.of(query.currencyCode());
        Map<String, String> attributes = query.attributes() == null ? Map.of() : query.attributes();
        PricingContext context = new PricingContext(currencyCode, query.quantity(), attributes);
        List<PriceCandidate> candidates = priceQueryPort.findCandidates(
                query.priceSetIds(),
                currencyCode.value()
        );
        List<PricePreferenceView> preferences = priceQueryPort.findPreferences();
        List<CalculatedPriceSet> calculated = calculatePricesPolicy.calculate(
                query.priceSetIds(),
                context,
                candidates,
                preferences,
                Instant.now()
        );
        return calculated.stream().map(PricingResultMapper::toCalculatedResult).toList();
    }
}
