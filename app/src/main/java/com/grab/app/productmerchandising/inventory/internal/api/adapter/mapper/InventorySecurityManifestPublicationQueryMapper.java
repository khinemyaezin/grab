package com.grab.app.productmerchandising.inventory.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.productmerchandising.inventory.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.app.productmerchandising.inventory.port.InventorySecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InventorySecurityManifestPublicationQueryMapper {
    public abstract InventorySecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
