package com.grab.store.identity.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.command.AuthResult;
import com.grab.store.identity.internal.command.SwitchAccessContextCommand;
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
