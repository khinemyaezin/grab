package com.grab.store.pricing.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.store.pricing.internal.api.rest.dto.response.VariantPriceSetLinkResponse;
import com.grab.store.pricing.internal.api.rest.mapper.ListVariantPriceSetLinksRequestMapper;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.query.VariantPriceSetLinkResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@PricingEnabled
@RequiredArgsConstructor
public class VariantPriceSetLinkQueryService {
    private static final Logger log = Loggers.getLogger(VariantPriceSetLinkQueryService.class);

    private final QueryBus queryBus;
    private final ListVariantPriceSetLinksRequestMapper listVariantPriceSetLinksRequestMapper;

    public List<VariantPriceSetLinkResponse> findByVariantIds(Collection<String> variantIds) {
        log.info("Listing variant price set links for {} variant(s)", variantIds.size());
        List<VariantPriceSetLinkResult> results = queryBus.dispatch(
                listVariantPriceSetLinksRequestMapper.toQuery(List.copyOf(variantIds))
        );
        return listVariantPriceSetLinksRequestMapper.toResponseList(results);
    }
}
