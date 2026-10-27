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
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response.PriceSetResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.mapper.GetPriceSetRequestMapper;
import com.pricing.application.model.write.PriceSetResult;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
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
