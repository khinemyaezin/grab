package com.grab.store.pricing.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.grab.store.pricing.internal.api.rest.mapper.GetPriceListRequestMapper;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.query.ListPriceListsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class PriceListQueryService {
    private static final Logger log = Loggers.getLogger(PriceListQueryService.class);

    private final QueryBus queryBus;
    private final GetPriceListRequestMapper getPriceListRequestMapper;

    public PriceListResponse getPriceList(String priceListId) {
        log.info("Getting price list {}", priceListId);
        PriceListResult result = queryBus.dispatch(getPriceListRequestMapper.toQuery(priceListId));
        return getPriceListRequestMapper.toResponse(result);
    }

    public List<PriceListResponse> listPriceLists() {
        List<PriceListResult> results = queryBus.dispatch(new ListPriceListsQuery());
        return results.stream().map(getPriceListRequestMapper::toResponse).toList();
    }
}
