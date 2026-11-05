package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.RolePermissionPort.CreateRoleMessage;
import com.grab.app.account.identity.port.message.RolePermissionPort.RoleItemResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.CreateRoleCommand;
import com.identity.application.model.write.RoleResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateRolePortMapper {

    public abstract CreateRoleCommand toCommand(CreateRoleMessage message);

    public abstract RoleItemResult toResponse(RoleResult result);
}

