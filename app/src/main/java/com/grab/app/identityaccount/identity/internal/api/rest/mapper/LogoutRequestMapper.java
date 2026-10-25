package com.grab.app.identityaccount.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.identityaccount.identity.internal.api.rest.dto.request.LogoutRequest;
import com.identity.application.model.write.LogoutCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LogoutRequestMapper {
    public abstract LogoutCommand toCommand(LogoutRequest request);
}
