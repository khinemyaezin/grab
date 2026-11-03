package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.account.identity.port.message.AccessAssignmentPort.AuthTokenResult;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.SwitchContextMessage;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.SwitchAccessContextCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SwitchAccessContextPortMapper {

    public SwitchAccessContextCommand toCommand(SwitchContextMessage message) {
        if (message == null) {
            return null;
        }
        return new SwitchAccessContextCommand(
                message.userId(),
                message.targetAssignmentId(),
                message.currentRefreshToken()
        );
    }

    public abstract AuthTokenResult toResponse(AuthResult result);
}
