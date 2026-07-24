package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.internal.query.ListPriceListsQuery;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class ListPriceListsQueryHandler
        implements QueryHandler<ListPriceListsQuery, List<PriceListResult>> {

    private final PriceListRepository priceListRepository;

    @Override
    @PricingReadTransactional
    public List<PriceListResult> handle(ListPriceListsQuery query) {
        return priceListRepository.findAll().stream()
                .map(PricingResultMapper::toPriceListResult)
                .toList();
    }

    @Override
    public Class<ListPriceListsQuery> getQueryType() {
        return ListPriceListsQuery.class;
    }
}
