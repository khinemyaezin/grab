package com.grab.service.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.channel.saleschannel.SalesChannelManagementPort.ChannelMessageResult;
import com.grab.service.channel.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSalesChannelPortMapper {

    public GetSalesChannelQuery toQuery(String salesChannelId, String merchantId) {
        return new GetSalesChannelQuery(salesChannelId, merchantId);
    }

    public abstract ChannelMessageResult toResponse(SalesChannelResult result);
}

