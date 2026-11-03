package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.AuthenticationPort.RegisterUserMessage;
import com.grab.app.account.identity.port.message.AuthenticationPort.UserRegistrationResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RegisterUserPortMapper {

    public abstract RegisterCommand toCommand(RegisterUserMessage message);

    public abstract UserRegistrationResult toResponse(UserProfileResult result);
}

