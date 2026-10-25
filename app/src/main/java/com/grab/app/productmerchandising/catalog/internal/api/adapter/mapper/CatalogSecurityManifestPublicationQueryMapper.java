package com.grab.app.productmerchandising.catalog.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.productmerchandising.catalog.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.app.productmerchandising.catalog.port.CatalogSecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CatalogSecurityManifestPublicationQueryMapper {
    public abstract CatalogSecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
