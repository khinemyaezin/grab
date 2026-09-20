package com.grab.store.saleschannel.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.saleschannel.internal.api.rest.dto.response.SalesChannelResponse;
import com.grab.store.saleschannel.internal.api.rest.mapper.GetSalesChannelDtoMapper;
import com.grab.store.saleschannel.internal.api.rest.mapper.ListSalesChannelsDtoMapper;
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
