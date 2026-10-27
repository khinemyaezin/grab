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
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request.CalculatePricesRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.CalculatedPriceSetResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.CalculatePricesRequestMapper;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
import com.pricing.application.model.read.CalculatedPriceSetResult;
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
