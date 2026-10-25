package com.grab.app.identityaccount.identity.internal.api.rest.mapper;

import com.grab.app.identityaccount.identity.internal.api.rest.dto.request.LoginRequest;
import com.grab.app.identityaccount.identity.internal.api.rest.dto.response.AuthResponse;
import com.identity.application.model.write.*;
import org.mapstruct.Mapper;

import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LoginRequestMapper {
    public abstract LoginCommand toCommand(LoginRequest request);

    public abstract AuthResponse toResponse(AuthResult result);
}
