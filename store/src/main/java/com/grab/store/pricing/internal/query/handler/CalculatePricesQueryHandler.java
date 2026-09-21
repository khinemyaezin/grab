package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.port.inbound.CalculatePricesUseCase;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CalculatePricesQueryHandler implements QueryHandler<CalculatePricesQuery, List<CalculatedPriceSetResult>> {

    private final CalculatePricesUseCase calculatePricesUseCase;

    @Override
    @PricingReadTransactional
    public List<CalculatedPriceSetResult> handle(CalculatePricesQuery query) {
        return calculatePricesUseCase.execute(query);
    }

    @Override
    public Class<CalculatePricesQuery> getQueryType() {
        return CalculatePricesQuery.class;
    }
}
