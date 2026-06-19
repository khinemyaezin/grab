package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.RegisterUserRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.command.*;
import org.mapstruct.Mapper;

import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RegisterUserRequestMapper {
    public abstract RegisterUserCommand toCommand(RegisterUserRequest request);

    public abstract AuthResponse toResponse(AuthResult result);
}
