package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.ListPricePreferencesQuery;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.port.inbound.ListPricePreferencesUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListPricePreferencesQueryHandler
        implements QueryHandler<ListPricePreferencesQuery, List<PricePreferenceResult>> {

    private final ListPricePreferencesUseCase listPricePreferencesUseCase;

    public ListPricePreferencesQueryHandler(ListPricePreferencesUseCase listPricePreferencesUseCase) {
        this.listPricePreferencesUseCase = listPricePreferencesUseCase;
    }

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

