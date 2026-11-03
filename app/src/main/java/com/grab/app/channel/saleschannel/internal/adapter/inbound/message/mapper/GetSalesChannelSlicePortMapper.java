package com.grab.app.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.SalesChannelContextPort.SalesChannelSliceResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSalesChannelSlicePortMapper {

    public GetSalesChannelQuery toQuery(Id salesChannelId) {
        return salesChannelId != null ? new GetSalesChannelQuery(salesChannelId.getValue(), null) : null;
    }

    public abstract SalesChannelSliceResult toResponse(SalesChannelResult result);
}

