package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.dto.response.RegionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.mapper.GetRegionRequestMapper;
import com.region.application.exception.RegionServiceError;
import com.region.application.exception.RegionServiceException;
import com.region.application.model.read.RegionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionQueryService {

    private final QueryBus queryBus;
    private final GetRegionRequestMapper getRegionRequestMapper;

    public RegionResponse get(String regionId) {
        Optional<RegionResult> result = queryBus.dispatch(getRegionRequestMapper.toQuery(regionId));
        return result.map(getRegionRequestMapper::toResponse)
                .orElseThrow(() -> new RegionServiceException(
                        new RegionServiceError.RegionNotFound(regionId),
                        "Region not found: " + regionId
                ));
    }
}
