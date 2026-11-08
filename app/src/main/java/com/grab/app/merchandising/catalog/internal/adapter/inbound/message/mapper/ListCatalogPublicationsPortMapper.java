package com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.CatalogPublicationItem;
import com.catalog.application.model.read.ListCatalogPublicationsQuery;
import com.grab.app.merchandising.catalog.port.message.CatalogDiscoveryPort.PublicationSliceResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListCatalogPublicationsPortMapper {

    public ListCatalogPublicationsQuery toQuery() {
        return new ListCatalogPublicationsQuery();
    }

    public abstract PublicationSliceResult toResponse(CatalogPublicationItem item);
}

