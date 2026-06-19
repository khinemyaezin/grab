package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.request.LogoutRequest;
import com.grab.store.identity.internal.command.LogoutCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LogoutRequestMapper {
    public abstract LogoutCommand toCommand(LogoutRequest request);
}
