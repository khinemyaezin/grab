package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AuthResponse;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.SwitchAccessContextCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SwitchAccessContextRequestMapper {
    public abstract SwitchAccessContextCommand toCommand(
            String userId,
            String assignmentId,
            String currentRefreshToken
    );

    public abstract AuthResponse toResponse(AuthResult result);
}
