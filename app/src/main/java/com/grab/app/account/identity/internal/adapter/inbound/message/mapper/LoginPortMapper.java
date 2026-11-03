package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.AuthenticationPort.AuthTokensResult;
import com.grab.app.account.identity.port.message.AuthenticationPort.LoginMessage;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.LoginCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LoginPortMapper {

    public abstract LoginCommand toCommand(LoginMessage message);

    public abstract AuthTokensResult toResponse(AuthResult result);
}

