package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.ListPriceListsQuery;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.ListPriceListsUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListPriceListsQueryHandler implements QueryHandler<ListPriceListsQuery, List<PriceListResult>> {

    private final ListPriceListsUseCase listPriceListsUseCase;

    public ListPriceListsQueryHandler(ListPriceListsUseCase listPriceListsUseCase) {
        this.listPriceListsUseCase = listPriceListsUseCase;
    }

    @Override
    @PricingReadTransactional
    public List<PriceListResult> handle(ListPriceListsQuery query) {
        return listPriceListsUseCase.execute(query);
    }

    @Override
    public Class<ListPriceListsQuery> getQueryType() {
        return ListPriceListsQuery.class;
    }
}

