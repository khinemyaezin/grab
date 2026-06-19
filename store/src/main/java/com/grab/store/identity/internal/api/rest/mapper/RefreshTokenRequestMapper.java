package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.RefreshTokenRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.command.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class RefreshTokenRequestMapper {
    public abstract RefreshTokenCommand toCommand(RefreshTokenRequest request);

    public abstract AuthResponse toResponse(AuthResult result);
}
