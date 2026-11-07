package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.KeyResolutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerSalesChannelMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerSalesChannelsMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ResolveSellerPublishableKeyMapper;
import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.GetSellerSalesChannelQuery;
import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.ListSellerSalesChannelsQuery;
import com.grab.app.platform.seller.internal.query.model.SellerSalesChannelQueries.ResolveSellerPublishableKeyQuery;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.ChannelOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.KeyResolutionOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerSalesChannelQueryService {

    private final QueryBus queryBus;
    private final GetSellerSalesChannelMapper getChannelMapper;
    private final ListSellerSalesChannelsMapper listChannelsMapper;
    private final ResolveSellerPublishableKeyMapper resolveKeyMapper;

    public SellerSalesChannelQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerSalesChannelMapper getChannelMapper,
            ListSellerSalesChannelsMapper listChannelsMapper,
            ResolveSellerPublishableKeyMapper resolveKeyMapper
    ) {
        this.queryBus = queryBus;
        this.getChannelMapper = getChannelMapper;
        this.listChannelsMapper = listChannelsMapper;
        this.resolveKeyMapper = resolveKeyMapper;
    }

    public Optional<SalesChannelResponse> getChannelById(String salesChannelId, String merchantId) {
        GetSellerSalesChannelQuery query = getChannelMapper.toQuery(salesChannelId, merchantId);
        Optional<ChannelOutboundResult> result = queryBus.dispatch(query);
        return result.map(getChannelMapper::toResponse);
    }

    public List<SalesChannelResponse> listChannels(String merchantId, int page, int size) {
        ListSellerSalesChannelsQuery query = listChannelsMapper.toQuery(merchantId, page, size);
        List<ChannelOutboundResult> results = queryBus.dispatch(query);
        return listChannelsMapper.toResponse(results);
    }

    public Optional<KeyResolutionResponse> resolveKey(String rawToken) {
        ResolveSellerPublishableKeyQuery query = resolveKeyMapper.toQuery(rawToken);
        Optional<KeyResolutionOutboundResult> result = queryBus.dispatch(query);
        return result.map(resolveKeyMapper::toResponse);
    }
}
