package com.grab.app.catalog.inventory.internal.adapter.outbound.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.CentralMapperConfig;
import com.grab.app.catalog.inventory.port.InventorySecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InventorySecurityManifestPublicationQueryMapper {
    public abstract InventorySecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
