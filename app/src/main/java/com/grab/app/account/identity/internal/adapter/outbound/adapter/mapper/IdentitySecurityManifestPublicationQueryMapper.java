package com.grab.app.account.identity.internal.adapter.outbound.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.CentralMapperConfig;
import com.grab.app.account.identity.port.IdentitySecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class IdentitySecurityManifestPublicationQueryMapper {
    public abstract IdentitySecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
