package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.LoginRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.command.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class LoginRequestMapper {
    public abstract LoginCommand toCommand(LoginRequest request);

    public abstract AuthResponse toResponse(AuthResult result);
}
