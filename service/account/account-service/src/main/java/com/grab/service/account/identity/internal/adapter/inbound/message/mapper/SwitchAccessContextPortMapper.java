package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.AccessAssignmentPort.AuthTokenResult;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.SwitchContextMessage;
import com.grab.service.account.internal.config.CentralMapperConfig;
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
