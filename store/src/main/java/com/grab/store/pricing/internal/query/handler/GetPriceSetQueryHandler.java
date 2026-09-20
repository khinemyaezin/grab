package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.port.inbound.GetPriceSetUseCase;
import com.pricing.application.model.read.GetPriceSetQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPriceSetQueryHandler implements QueryHandler<GetPriceSetQuery, PriceSetResult> {

    private final GetPriceSetUseCase getPriceSetUseCase;

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
