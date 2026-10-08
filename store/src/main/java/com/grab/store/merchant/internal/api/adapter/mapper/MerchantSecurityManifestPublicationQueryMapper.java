package com.grab.store.merchant.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.security.SecurityManifestPublicationQueryPort.PublicationStatus;
import com.grab.store.merchant.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.merchant.port.MerchantSecurityManifestPublicationQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class MerchantSecurityManifestPublicationQueryMapper {
    public abstract MerchantSecurityManifestPublicationQuery.PublicationStatus toResponse(PublicationStatus view);
}
