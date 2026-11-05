package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.GetPriceSetQuery;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.port.inbound.GetPriceSetUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetPriceSetQueryHandler implements QueryHandler<GetPriceSetQuery, PriceSetResult> {

    private final GetPriceSetUseCase getPriceSetUseCase;

    public GetPriceSetQueryHandler(GetPriceSetUseCase getPriceSetUseCase) {
        this.getPriceSetUseCase = getPriceSetUseCase;
    }

    @Override
    @PricingReadTransactional
    public PriceSetResult handle(GetPriceSetQuery query) {
        return getPriceSetUseCase.execute(query);
    }

    @Override
    public Class<GetPriceSetQuery> getQueryType() {
        return GetPriceSetQuery.class;
    }
}

