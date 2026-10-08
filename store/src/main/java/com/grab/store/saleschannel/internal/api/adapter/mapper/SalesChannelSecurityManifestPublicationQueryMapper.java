package com.grab.store.saleschannel.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.store.saleschannel.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.saleschannel.port.SalesChannelSecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SalesChannelSecurityManifestPublicationQueryMapper {
    public abstract SalesChannelSecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
