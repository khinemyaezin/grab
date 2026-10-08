package com.grab.store.identity.internal.api.adapter.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.mapper.CentralMapperConfig;
import com.grab.store.identity.port.SecurityCatalogQuery.SecurityCatalogStatus;
import com.identity.application.model.read.SecurityCatalogStatusView;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SecurityCatalogQueryMapper {
    public abstract SecurityCatalogStatus toResponse(SecurityCatalogStatusView view);
}
