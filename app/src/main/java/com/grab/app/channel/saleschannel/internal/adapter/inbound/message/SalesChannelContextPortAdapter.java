package com.grab.app.channel.saleschannel.internal.adapter.inbound.message;

import com.grab.app.channel.saleschannel.internal.adapter.inbound.message.mapper.GetSalesChannelSlicePortMapper;
import com.grab.app.channel.saleschannel.port.message.SalesChannelContextPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.saleschannel.application.model.read.CheckSalesChannelUsableQuery;
import com.saleschannel.application.model.read.CheckSalesChannelUsableResult;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.ListEnabledSalesChannelRegionsQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class SalesChannelContextPortAdapter implements SalesChannelContextPort {

    private final QueryBus queryBus;
    private final GetSalesChannelSlicePortMapper getSliceMapper;

    public SalesChannelContextPortAdapter(
            @Qualifier("channelQueryBus") QueryBus queryBus,
            GetSalesChannelSlicePortMapper getSliceMapper) {
        this.queryBus = queryBus;
        this.getSliceMapper = getSliceMapper;
    }

    @Override
    public Optional<SalesChannelSliceResult> findChannelSlice(Id salesChannelId) {
        if (salesChannelId == null) {
            return Optional.empty();
        }
        GetSalesChannelQuery query = getSliceMapper.toQuery(salesChannelId);
        SalesChannelResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getSliceMapper.toResponse(result));
    }

    @Override
    public boolean isEnabled(Id salesChannelId) {
        if (salesChannelId == null) {
            return false;
        }
        CheckSalesChannelUsableQuery query = new CheckSalesChannelUsableQuery(salesChannelId.getValue(), null);
        CheckSalesChannelUsableResult usable = queryBus.dispatch(query);
        return usable != null && usable.usable();
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
