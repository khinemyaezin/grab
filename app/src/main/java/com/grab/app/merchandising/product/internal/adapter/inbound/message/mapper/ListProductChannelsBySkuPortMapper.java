package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.ListPublishedProductChannelsBySkuQuery;
import com.catalog.application.model.read.PublishedProductChannel;
import com.grab.app.merchandising.product.port.message.CatalogDiscoveryPort.ProductChannelPairResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListProductChannelsBySkuPortMapper {

    public ListPublishedProductChannelsBySkuQuery toQuery(String sku) {
        return new ListPublishedProductChannelsBySkuQuery(sku);
    }

    public abstract ProductChannelPairResult toResponse(PublishedProductChannel result);
}

