package com.grab.store.saleschannel.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.saleschannel.internal.api.rest.dto.response.SalesChannelResponse;
import com.grab.store.saleschannel.internal.query.GetSalesChannelQuery;
import com.grab.store.saleschannel.internal.query.ListSalesChannelsQuery;
import com.grab.store.saleschannel.internal.query.SalesChannelResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalesChannelQueryService {

    private final QueryBus queryBus;
    private final AuthenticatedSalesChannelMerchantResolver merchantResolver;

    public Page<SalesChannelResponse> list(Pageable pageable) {
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        Page<SalesChannelResult> results = queryBus.dispatch(new ListSalesChannelsQuery(merchantId, pageable));
        return results.map(this::toResponse);
    }

    public SalesChannelResponse get(String salesChannelId) {
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        SalesChannelResult result = queryBus.dispatch(new GetSalesChannelQuery(salesChannelId, merchantId));
        return toResponse(result);
    }

    private SalesChannelResponse toResponse(SalesChannelResult result) {
        return new SalesChannelResponse(
                result.salesChannelId(),
                result.name(),
                result.type(),
                result.owner(),
                result.merchantId(),
                result.status()
        );
    }
}
