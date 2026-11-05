package com.grab.app.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.ChangeUserStatusCommand;
import com.identity.domain.enums.UserStatus;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeUserStatusPortMapper {

    public ChangeUserStatusCommand toCommand(Id userId, String newStatus) {
        return new ChangeUserStatusCommand(userId, UserStatus.valueOf(newStatus));
    }
}

