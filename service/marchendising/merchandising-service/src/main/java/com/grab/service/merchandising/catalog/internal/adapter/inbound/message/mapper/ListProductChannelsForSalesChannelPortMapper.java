package com.grab.service.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.ListPublishedProductChannelsForSalesChannelAfterQuery;
import com.catalog.application.model.read.PublishedProductChannel;
import com.grab.service.contract.message.merchandising.catalog.CatalogDiscoveryPort.ProductChannelPairResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListProductChannelsForSalesChannelPortMapper {

    public ListPublishedProductChannelsForSalesChannelAfterQuery toQuery(Id salesChannelId, Id afterProductId, int limit) {
        return new ListPublishedProductChannelsForSalesChannelAfterQuery(salesChannelId, afterProductId, limit);
    }

    public abstract ProductChannelPairResult toResponse(PublishedProductChannel result);
}
