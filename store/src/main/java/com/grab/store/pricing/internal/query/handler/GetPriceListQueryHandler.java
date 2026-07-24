package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.query.GetPriceListQuery;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class GetPriceListQueryHandler implements QueryHandler<GetPriceListQuery, PriceListResult> {

    private final PriceListRepository priceListRepository;

    @Override
    @PricingReadTransactional
    public PriceListResult handle(GetPriceListQuery query) {
        return priceListRepository.findById(query.priceListId())
                .map(PricingResultMapper::toPriceListResult)
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(query.priceListId().getValue()),
                        "Price list not found"
                ));
    }

    @Override
    public Class<GetPriceListQuery> getQueryType() {
        return GetPriceListQuery.class;
    }
}
