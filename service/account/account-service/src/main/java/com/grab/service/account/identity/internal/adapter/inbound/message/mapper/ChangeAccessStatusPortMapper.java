package com.grab.service.account.identity.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.identity.AccessAssignmentPort.AssignmentMessageResult;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.ChangeAssignmentStatusMessage;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.ChangeAccessStatusCommand;
import com.identity.domain.enums.AccessAssignmentStatus;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ChangeAccessStatusPortMapper {

    public ChangeAccessStatusCommand toCommand(ChangeAssignmentStatusMessage message) {
        if (message == null) {
            return null;
        }
        return new ChangeAccessStatusCommand(
                message.assignmentId(),
                AccessAssignmentStatus.valueOf(message.requestedStatus()),
                message.actorScopeKey(),
                message.actorScopeId(),
                message.actorUserId(),
                message.actorRoleCodes()
        );
    }

    public abstract AssignmentMessageResult toResponse(AccessAssignmentResult result);
}
