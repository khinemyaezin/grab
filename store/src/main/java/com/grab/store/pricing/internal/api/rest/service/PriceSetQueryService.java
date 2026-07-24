package com.grab.store.pricing.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceSetResponse;
import com.grab.store.pricing.internal.api.rest.mapper.GetPriceSetRequestMapper;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class PriceSetQueryService {
    private static final Logger log = Loggers.getLogger(PriceSetQueryService.class);

    private final QueryBus queryBus;
    private final GetPriceSetRequestMapper getPriceSetRequestMapper;

    public PriceSetResponse getPriceSet(String priceSetId) {
        log.info("Getting price set {}", priceSetId);
        PriceSetResult result = queryBus.dispatch(getPriceSetRequestMapper.toQuery(priceSetId));
        return getPriceSetRequestMapper.toResponse(result);
    }
}
