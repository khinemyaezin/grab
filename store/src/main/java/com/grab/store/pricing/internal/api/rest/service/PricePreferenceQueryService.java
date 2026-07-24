package com.grab.store.pricing.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.store.pricing.internal.api.rest.dto.response.PricePreferenceResponse;
import com.grab.store.pricing.internal.api.rest.mapper.GetPricePreferenceRequestMapper;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.query.ListPricePreferencesQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class PricePreferenceQueryService {
    private static final Logger log = Loggers.getLogger(PricePreferenceQueryService.class);

    private final QueryBus queryBus;
    private final GetPricePreferenceRequestMapper getPricePreferenceRequestMapper;

    public PricePreferenceResponse get(String pricePreferenceId) {
        log.info("Getting price preference {}", pricePreferenceId);
        PricePreferenceResult result = queryBus.dispatch(getPricePreferenceRequestMapper.toQuery(pricePreferenceId));
        return getPricePreferenceRequestMapper.toResponse(result);
    }

    public List<PricePreferenceResponse> list() {
        List<PricePreferenceResult> results = queryBus.dispatch(new ListPricePreferencesQuery());
        return results.stream().map(getPricePreferenceRequestMapper::toResponse).toList();
    }
}
