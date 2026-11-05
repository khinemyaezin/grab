package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.GetPriceListQuery;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.GetPriceListUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetPriceListQueryHandler implements QueryHandler<GetPriceListQuery, PriceListResult> {

    private final GetPriceListUseCase getPriceListUseCase;

    public GetPriceListQueryHandler(GetPriceListUseCase getPriceListUseCase) {
        this.getPriceListUseCase = getPriceListUseCase;
    }

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

