package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.ListPublishedProductChannelsAfterQuery;
import com.catalog.application.model.read.PublishedProductChannel;
import com.grab.app.merchandising.catalog.port.message.CatalogDiscoveryPort.ProductChannelPairResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListProductChannelsAfterPortMapper {

    public ListPublishedProductChannelsAfterQuery toQuery(Id productId, Id salesChannelId, int limit) {
        return new ListPublishedProductChannelsAfterQuery(productId, salesChannelId, limit);
    }

    public abstract ProductChannelPairResult toResponse(PublishedProductChannel item);
}

