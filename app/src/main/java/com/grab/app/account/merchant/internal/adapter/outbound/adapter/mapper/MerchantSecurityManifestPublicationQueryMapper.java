package com.grab.app.account.merchant.internal.adapter.outbound.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.CentralMapperConfig;
import com.grab.app.account.merchant.port.MerchantSecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class MerchantSecurityManifestPublicationQueryMapper {
    public abstract MerchantSecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
