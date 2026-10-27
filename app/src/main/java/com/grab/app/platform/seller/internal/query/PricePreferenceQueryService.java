package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.GetPricePreferenceRequestMapper;
import com.pricing.application.model.write.PricePreferenceResult;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
import com.pricing.application.model.read.ListPricePreferencesQuery;
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
