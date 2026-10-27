package com.grab.app.account.identity.internal.adapter.outbound.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper.CentralMapperConfig;
import com.grab.app.account.identity.port.SecurityCatalogQuery.SecurityCatalogStatus;
import com.identity.application.model.read.SecurityCatalogStatusView;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SecurityCatalogQueryMapper {
    public abstract SecurityCatalogStatus toResponse(SecurityCatalogStatusView view);
}
