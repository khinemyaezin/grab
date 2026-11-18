package com.grab.service.channel.saleschannel.internal.adapter.inbound.message;

import com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper.GetSalesChannelSlicePortMapper;
import com.grab.service.contract.message.channel.saleschannel.SalesChannelContextPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.service.channel.saleschannel.internal.config.SalesChannelReadTransactional;
import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.model.read.FindSalesChannelSliceResult;
import com.saleschannel.application.port.inbound.FindSalesChannelSliceUseCase;
import com.saleschannel.application.model.read.ListEnabledSalesChannelRegionsQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RmiExport
public class SalesChannelContextPortAdapter implements SalesChannelContextPort {

    private final QueryBus queryBus;
    private final GetSalesChannelSlicePortMapper getSliceMapper;
    private final FindSalesChannelSliceUseCase findSlice;

    public SalesChannelContextPortAdapter(
            @Qualifier("channelQueryBus") QueryBus queryBus,
            GetSalesChannelSlicePortMapper getSliceMapper,
            FindSalesChannelSliceUseCase findSlice) {
        this.queryBus = queryBus;
        this.getSliceMapper = getSliceMapper;
        this.findSlice = findSlice;
    }

    @Override
    @SalesChannelReadTransactional
    public SalesChannelSliceResult findChannelSlice(Id salesChannelId) {
        if (salesChannelId == null) {
            return null;
        }
        FindSalesChannelSliceQuery query = getSliceMapper.toQuery(salesChannelId);
        Optional<FindSalesChannelSliceResult> result = findSlice.execute(query);
        return result.map(getSliceMapper::toResponse).orElse(null);
    }

    @Override
    @SalesChannelReadTransactional
    public boolean isEnabled(Id salesChannelId) {
        if (salesChannelId == null) {
            return false;
        }
        FindSalesChannelSliceQuery query = getSliceMapper.toQuery(salesChannelId);
        Optional<FindSalesChannelSliceResult> result = findSlice.execute(query);
        return result.map(FindSalesChannelSliceResult::enabled).orElse(false);
    }

    @Override
    public List<Id> findRegionsForChannel(Id salesChannelId) {
        if (salesChannelId == null) {
            return Collections.emptyList();
        }
        ListEnabledSalesChannelRegionsQuery query = new ListEnabledSalesChannelRegionsQuery(salesChannelId);
        List<Id> regionIds = queryBus.dispatch(query);
        if (regionIds == null) {
            return Collections.emptyList();
        }
        return regionIds;
    }
}
