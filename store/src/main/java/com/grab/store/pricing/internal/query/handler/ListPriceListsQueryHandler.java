package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.ListPriceListsUseCase;
import com.pricing.application.model.read.ListPriceListsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListPriceListsQueryHandler implements QueryHandler<ListPriceListsQuery, List<PriceListResult>> {

    private final ListPriceListsUseCase listPriceListsUseCase;

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
