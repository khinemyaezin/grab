package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.dto.response.RegionCountryResponse;
import com.region.application.model.read.ListActiveCountryCodesQuery;
import com.grab.framework.cqrs.query.QueryBus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionCountryQueryService {
    private final QueryBus queryBus;

    public List<RegionCountryResponse> list() {
        return queryBus.dispatch(new ListActiveCountryCodesQuery()).stream()
                .map(RegionCountryResponse::new)
                .toList();
    }
}
