package com.grab.store.saleschannel.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.saleschannel.internal.api.rest.dto.response.SalesChannelResponse;
import com.saleschannel.application.model.read.ListSalesChannelsQuery;
import com.saleschannel.application.model.read.SalesChannelResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListSalesChannelsDtoMapper {

    public ListSalesChannelsQuery toQuery(String merchantId, Pageable pageable) {
        return new ListSalesChannelsQuery(merchantId, pageable);
    }

    public abstract SalesChannelResponse toResponse(SalesChannelResult result);
}
