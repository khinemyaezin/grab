package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.port.inbound.GetPricePreferenceUseCase;
import com.pricing.application.model.read.GetPricePreferenceQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPricePreferenceQueryHandler implements QueryHandler<GetPricePreferenceQuery, PricePreferenceResult> {

    private final GetPricePreferenceUseCase getPricePreferenceUseCase;

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
