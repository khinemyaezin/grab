package com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response.SalesChannelResponse;
import com.saleschannel.application.model.read.GetSalesChannelQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetSalesChannelDtoMapper {

    public GetSalesChannelQuery toQuery(String merchantId, String salesChannelId) {
        return new GetSalesChannelQuery(salesChannelId, merchantId);
    }

    public abstract SalesChannelResponse toResponse(SalesChannelResult result);
}
