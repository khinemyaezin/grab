package com.grab.app.channel.saleschannel.internal.adapter.outbound.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.mapper.CentralMapperConfig;
import com.grab.app.channel.saleschannel.port.SalesChannelSecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SalesChannelSecurityManifestPublicationQueryMapper {
    public abstract SalesChannelSecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
