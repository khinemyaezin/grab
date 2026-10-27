package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.LoginRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AuthResponse;
import com.identity.application.model.write.*;
import org.mapstruct.Mapper;

import com.grab.framework.mapper.IdMapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LoginRequestMapper {
    public abstract LoginCommand toCommand(LoginRequest request);

    public abstract AuthResponse toResponse(AuthResult result);
}
