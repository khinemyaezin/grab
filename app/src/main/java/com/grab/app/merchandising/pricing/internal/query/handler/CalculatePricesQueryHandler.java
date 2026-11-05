package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.CalculatePricesQuery;
import com.pricing.application.model.read.CalculatedPriceSetResult;
import com.pricing.application.port.inbound.CalculatePricesUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CalculatePricesQueryHandler implements QueryHandler<CalculatePricesQuery, List<CalculatedPriceSetResult>> {

    private final CalculatePricesUseCase calculatePricesUseCase;

    public CalculatePricesQueryHandler(CalculatePricesUseCase calculatePricesUseCase) {
        this.calculatePricesUseCase = calculatePricesUseCase;
    }

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

