package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.RegisterRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.command.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class RegisterUserRequestMapper {
    public abstract RegisterUserCommand toCommand(RegisterRequest request);

    public abstract AuthResponse toResponse(AuthResult result);
}
