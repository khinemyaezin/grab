package com.grab.store.identity.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.store.identity.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.identity.port.IdentitySecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class IdentitySecurityManifestPublicationQueryMapper {
    public abstract IdentitySecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
