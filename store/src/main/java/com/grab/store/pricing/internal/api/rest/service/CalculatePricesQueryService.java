package com.grab.store.pricing.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.store.pricing.internal.api.rest.dto.request.CalculatePricesRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.CalculatedPriceSetResponse;
import com.grab.store.pricing.internal.api.rest.mapper.CalculatePricesRequestMapper;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.query.CalculatedPriceSetResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class CalculatePricesQueryService {
    private static final Logger log = Loggers.getLogger(CalculatePricesQueryService.class);

    private final QueryBus queryBus;
    private final CalculatePricesRequestMapper calculatePricesRequestMapper;

    public List<CalculatedPriceSetResponse> calculate(CalculatePricesRequest request) {
        log.info("Calculating prices for {} price sets", request.priceSetIds().size());
        List<CalculatedPriceSetResult> results = queryBus.dispatch(calculatePricesRequestMapper.toQuery(request));
        return calculatePricesRequestMapper.toResponseList(results);
    }
}
