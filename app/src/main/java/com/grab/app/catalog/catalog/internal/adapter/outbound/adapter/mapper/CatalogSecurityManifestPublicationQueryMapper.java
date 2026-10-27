package com.grab.app.catalog.catalog.internal.adapter.outbound.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.mapper.CentralMapperConfig;
import com.grab.app.catalog.catalog.port.CatalogSecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CatalogSecurityManifestPublicationQueryMapper {
    public abstract CatalogSecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
