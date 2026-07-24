package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.query.GetPricePreferenceQuery;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.repository.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class GetPricePreferenceQueryHandler
        implements QueryHandler<GetPricePreferenceQuery, PricePreferenceResult> {

    private final PricePreferenceRepository pricePreferenceRepository;

    @Override
    @PricingReadTransactional
    public PricePreferenceResult handle(GetPricePreferenceQuery query) {
        return pricePreferenceRepository.findById(query.pricePreferenceId())
                .map(PricingResultMapper::toPreferenceResult)
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PricePreferenceNotFound(query.pricePreferenceId().getValue()),
                        "Price preference not found"
                ));
    }

    @Override
    public Class<GetPricePreferenceQuery> getQueryType() {
        return GetPricePreferenceQuery.class;
    }
}
