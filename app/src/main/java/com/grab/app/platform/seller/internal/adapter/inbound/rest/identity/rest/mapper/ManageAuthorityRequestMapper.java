package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.RoleResponse;
import com.identity.application.model.write.ManageAuthorityCommand;
import com.identity.application.model.write.RoleResult;
import org.mapstruct.Mapper;
import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ManageAuthorityRequestMapper {
    public abstract ManageAuthorityCommand toCommand(String roleCode, String authorityCode, boolean assign);
    public abstract RoleResponse toResponse(RoleResult result);
}
