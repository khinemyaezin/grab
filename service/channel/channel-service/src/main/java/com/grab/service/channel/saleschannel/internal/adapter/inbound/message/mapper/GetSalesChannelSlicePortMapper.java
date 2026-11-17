package com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.channel.saleschannel.SalesChannelContextPort.SalesChannelSliceResult;
import com.grab.service.channel.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.read.FindSalesChannelSliceQuery;
import com.saleschannel.application.model.read.FindSalesChannelSliceResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSalesChannelSlicePortMapper {

    public FindSalesChannelSliceQuery toQuery(Id salesChannelId) {
        return salesChannelId != null ? new FindSalesChannelSliceQuery(salesChannelId.getValue()) : null;
    }

    @Mapping(target = "channelType", source = "type")
    public abstract SalesChannelSliceResult toResponse(FindSalesChannelSliceResult result);
}

