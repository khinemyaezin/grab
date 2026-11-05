package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.GetPricePreferenceQuery;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.port.inbound.GetPricePreferenceUseCase;
import org.springframework.stereotype.Component;

@Component
public class GetPricePreferenceQueryHandler implements QueryHandler<GetPricePreferenceQuery, PricePreferenceResult> {

    private final GetPricePreferenceUseCase getPricePreferenceUseCase;

    public GetPricePreferenceQueryHandler(GetPricePreferenceUseCase getPricePreferenceUseCase) {
        this.getPricePreferenceUseCase = getPricePreferenceUseCase;
    }

    @Override
    @PricingReadTransactional
    public PricePreferenceResult handle(GetPricePreferenceQuery query) {
        return getPricePreferenceUseCase.execute(query);
    }

    @Override
    public Class<GetPricePreferenceQuery> getQueryType() {
        return GetPricePreferenceQuery.class;
    }
}

