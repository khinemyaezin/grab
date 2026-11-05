package com.grab.app.channel.saleschannel.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.saleschannel.port.message.SalesChannelManagementPort.ChannelMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.saleschannel.application.model.read.ListSalesChannelsQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.PageRequest;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListSalesChannelsPortMapper {

    public ListSalesChannelsQuery toQuery(String merchantId, int page, int size) {
        return new ListSalesChannelsQuery(merchantId, PageRequest.of(page, size));
    }

    public abstract ChannelMessageResult toResponse(SalesChannelResult result);
}

