package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.grab.store.pricing.internal.query.ListPricePreferencesQuery;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.repository.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class ListPricePreferencesQueryHandler
        implements QueryHandler<ListPricePreferencesQuery, List<PricePreferenceResult>> {

    private final PricePreferenceRepository pricePreferenceRepository;

    @Override
    @PricingReadTransactional
    public List<PricePreferenceResult> handle(ListPricePreferencesQuery query) {
        return pricePreferenceRepository.findAll().stream()
                .map(PricingResultMapper::toPreferenceResult)
                .toList();
    }

    @Override
    public Class<ListPricePreferencesQuery> getQueryType() {
        return ListPricePreferencesQuery.class;
    }
}
