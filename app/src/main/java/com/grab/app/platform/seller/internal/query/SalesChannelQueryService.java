package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.mapper.GetSalesChannelDtoMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.mapper.ListSalesChannelsDtoMapper;
import com.saleschannel.application.model.read.SalesChannelResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalesChannelQueryService {

    private final QueryBus queryBus;
    private final AuthenticatedSalesChannelMerchantResolver merchantResolver;
    private final GetSalesChannelDtoMapper getSalesChannelDtoMapper;
    private final ListSalesChannelsDtoMapper listSalesChannelsDtoMapper;

    public Page<SalesChannelResponse> list(Pageable pageable) {
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        Page<SalesChannelResult> results = queryBus.dispatch(
                listSalesChannelsDtoMapper.toQuery(merchantId, pageable)
        );
        return results.map(listSalesChannelsDtoMapper::toResponse);
    }

    public SalesChannelResponse get(String salesChannelId) {
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        SalesChannelResult result = queryBus.dispatch(
                getSalesChannelDtoMapper.toQuery(merchantId, salesChannelId)
        );
        return getSalesChannelDtoMapper.toResponse(result);
    }
}
