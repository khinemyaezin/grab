package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.GetPriceListUseCase;
import com.pricing.application.model.read.GetPriceListQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPriceListQueryHandler implements QueryHandler<GetPriceListQuery, PriceListResult> {

    private final GetPriceListUseCase getPriceListUseCase;

    @Override
    @PricingReadTransactional
    public PriceListResult handle(GetPriceListQuery query) {
        return getPriceListUseCase.execute(query);
    }

    @Override
    public Class<GetPriceListQuery> getQueryType() {
        return GetPriceListQuery.class;
    }
}
