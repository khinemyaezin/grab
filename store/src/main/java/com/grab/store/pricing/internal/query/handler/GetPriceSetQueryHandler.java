package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.query.GetPriceSetQuery;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.repository.PriceSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class GetPriceSetQueryHandler implements QueryHandler<GetPriceSetQuery, PriceSetResult> {

    private final PriceSetRepository priceSetRepository;

    @Override
    @PricingReadTransactional
    public PriceSetResult handle(GetPriceSetQuery query) {
        return priceSetRepository.findById(query.priceSetId())
                .map(PricingResultMapper::toPriceSetResult)
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(query.priceSetId().getValue()),
                        "Price set not found"
                ));
    }

    @Override
    public Class<GetPriceSetQuery> getQueryType() {
        return GetPriceSetQuery.class;
    }
}
