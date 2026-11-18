package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.AuthenticationPort.LogoutMessage;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.LogoutCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LogoutPortMapper {

    public abstract LogoutCommand toCommand(LogoutMessage message);
}

