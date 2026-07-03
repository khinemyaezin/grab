package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.LoginRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.command.*;
import org.mapstruct.Mapper;

import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LoginRequestMapper {
    public abstract LoginCommand toCommand(LoginRequest request, String platformCode);

    public abstract AuthResponse toResponse(AuthResult result);
}
