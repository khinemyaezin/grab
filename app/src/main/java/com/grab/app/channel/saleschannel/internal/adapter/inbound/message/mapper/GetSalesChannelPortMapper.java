package com.grab.app.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.SalesChannelManagementPort.ChannelMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
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

