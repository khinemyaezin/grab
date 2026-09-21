package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.port.inbound.ListPricePreferencesUseCase;
import com.pricing.application.model.read.ListPricePreferencesQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListPricePreferencesQueryHandler implements QueryHandler<ListPricePreferencesQuery, List<PricePreferenceResult>> {

    private final ListPricePreferencesUseCase listPricePreferencesUseCase;

    @Override
    @PricingReadTransactional
    public List<PricePreferenceResult> handle(ListPricePreferencesQuery query) {
        return listPricePreferencesUseCase.execute(query);
    }

    @Override
    public Class<ListPricePreferencesQuery> getQueryType() {
        return ListPricePreferencesQuery.class;
    }
}
