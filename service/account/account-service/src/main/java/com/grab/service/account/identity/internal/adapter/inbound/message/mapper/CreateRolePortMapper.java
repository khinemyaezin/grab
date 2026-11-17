package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.RolePermissionPort.CreateRoleMessage;
import com.grab.service.contract.message.account.identity.RolePermissionPort.RoleItemResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.RoleResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateRolePortMapper {

    public abstract CreateRoleCommand toCommand(CreateRoleMessage message);

    public abstract RoleItemResult toResponse(RoleResult result);
}

