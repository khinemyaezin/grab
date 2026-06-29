package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.store.identity.internal.api.rest.dto.request.RegisterRequest;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.command.RegisterCommand;
import com.grab.store.identity.internal.command.UserProfileResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class RegisterRequestMapper {
    public abstract RegisterCommand toCommand(RegisterRequest request);
    public abstract UserProfileResponse toResponse(UserProfileResult result);
}
