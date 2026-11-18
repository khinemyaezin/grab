package com.grab.service.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.ListPublishedProductChannelsBySkuQuery;
import com.catalog.application.model.read.PublishedProductChannel;
import com.grab.service.contract.message.merchandising.catalog.CatalogDiscoveryPort.ProductChannelPairResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListProductChannelsBySkuPortMapper {

    public ListPublishedProductChannelsBySkuQuery toQuery(String sku) {
        return new ListPublishedProductChannelsBySkuQuery(sku);
    }

    public abstract ProductChannelPairResult toResponse(PublishedProductChannel result);
}

