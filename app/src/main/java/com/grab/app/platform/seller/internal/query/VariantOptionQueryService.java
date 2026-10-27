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
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.VariantOptionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.VariantOptionDtoMapper;
import com.catalog.application.model.read.GetVariantOptionsByNameQuery;
import com.catalog.application.model.read.VariantOptionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VariantOptionQueryService {

    private static final Logger log = Loggers.getLogger(VariantOptionQueryService.class);

    private final QueryBus queryBus;
    private final VariantOptionDtoMapper variantOptionDtoMapper;

    public VariantOptionResponse getVariantOptionsByName(String name, String typeId) {
        log.info("Getting variant options by name: {}", name);

        VariantOptionResult result = queryBus.dispatch(new GetVariantOptionsByNameQuery(name, typeId));
        return variantOptionDtoMapper.toResponse(result);
    }
}
