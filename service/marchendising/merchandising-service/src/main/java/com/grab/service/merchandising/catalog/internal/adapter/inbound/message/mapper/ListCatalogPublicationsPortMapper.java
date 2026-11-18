package com.grab.service.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.CatalogPublicationItem;
import com.catalog.application.model.read.ListCatalogPublicationsQuery;
import com.grab.service.contract.message.merchandising.catalog.CatalogDiscoveryPort.PublicationSliceResult;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListCatalogPublicationsPortMapper {

    public ListCatalogPublicationsQuery toQuery() {
        return new ListCatalogPublicationsQuery();
    }

    public abstract PublicationSliceResult toResponse(CatalogPublicationItem item);
}

