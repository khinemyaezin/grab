package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.LogoutRequest;
import com.identity.application.model.write.LogoutCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LogoutRequestMapper {
    public abstract LogoutCommand toCommand(LogoutRequest request);
}
