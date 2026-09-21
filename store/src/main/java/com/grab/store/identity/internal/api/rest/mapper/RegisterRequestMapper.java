package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.request.RegisterRequest;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.identity.application.model.write.RegisterCommand;
import com.identity.application.model.write.UserProfileResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RegisterRequestMapper {
    public abstract RegisterCommand toCommand(RegisterRequest request,String platformCode);
    public abstract UserProfileResponse toResponse(UserProfileResult result);
}
