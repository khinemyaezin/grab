package com.grab.app.identityaccount.merchant.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.app.identityaccount.merchant.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.app.identityaccount.merchant.port.MerchantSecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class MerchantSecurityManifestPublicationQueryMapper {
    public abstract MerchantSecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
