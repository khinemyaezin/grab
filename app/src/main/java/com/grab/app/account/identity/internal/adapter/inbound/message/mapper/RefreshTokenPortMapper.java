package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.AuthenticationPort.AuthTokensResult;
import com.grab.app.account.identity.port.message.AuthenticationPort.RefreshTokenMessage;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.RefreshTokenCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RefreshTokenPortMapper {

    public abstract RefreshTokenCommand toCommand(RefreshTokenMessage message);

    public abstract AuthTokensResult toResponse(AuthResult result);
}

