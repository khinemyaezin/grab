package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.AccessAssignmentPort.AssignAccessMessage;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.AssignmentMessageResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.GrantAccessCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GrantAccessPortMapper {

    public abstract GrantAccessCommand toCommand(AssignAccessMessage message);

    public abstract AssignmentMessageResult toResponse(AccessAssignmentResult result);
}

