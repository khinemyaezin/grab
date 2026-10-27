package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.VariantTypeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.VariantTypeDtoMapper;
import com.catalog.application.model.read.GetVariantTypesByNameQuery;
import com.catalog.application.model.read.VariantTypeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VariantTypeQueryService {

    private static final Logger log = Loggers.getLogger(VariantTypeQueryService.class);

    private final QueryBus queryBus;
    private final VariantTypeDtoMapper variantTypeDtoMapper;

    public VariantTypeResponse getVariantTypesByName(String name) {
        log.info("Getting variant types by name: {}", name);

        VariantTypeResult result = queryBus.dispatch(new GetVariantTypesByNameQuery(name));
        return variantTypeDtoMapper.toResponse(result);
    }
}
