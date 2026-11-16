package com.grab.app.account.identity.internal.adapter.inbound.message;

import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.ChangeAccessStatusPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.GrantAccessPortMapper;
import com.grab.app.account.identity.internal.adapter.inbound.message.mapper.SwitchAccessContextPortMapper;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.AccessWriteAcknowledgement;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.AssignAccessMessage;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.AssignmentMessageResult;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.AuthTokenResult;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.ChangeAssignmentStatusMessage;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.ReplaceAccessMessage;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.RevokeAccessMessage;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.RevokeSessionsByScopeMessage;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.SwitchContextMessage;
import com.grab.framework.cqrs.command.CommandBus;
import com.identity.application.model.write.AccessAssignmentResult;
import com.identity.application.model.write.AuthResult;
import com.identity.application.model.write.ChangeAccessStatusCommand;
import com.identity.application.model.write.GrantAccessCommand;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.identity.application.model.write.SwitchAccessContextCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AccessAssignmentWriteAdapter {

    private final CommandBus commandBus;
    private final GrantAccessPortMapper grantAccessMapper;
    private final ChangeAccessStatusPortMapper changeAccessStatusMapper;
    private final SwitchAccessContextPortMapper switchContextMapper;

    public AccessAssignmentWriteAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            GrantAccessPortMapper grantAccessMapper,
            ChangeAccessStatusPortMapper changeAccessStatusMapper,
            SwitchAccessContextPortMapper switchContextMapper
    ) {
        this.commandBus = commandBus;
        this.grantAccessMapper = grantAccessMapper;
        this.changeAccessStatusMapper = changeAccessStatusMapper;
        this.switchContextMapper = switchContextMapper;
    }

    public AssignmentMessageResult assignAccess(AssignAccessMessage message) {
        GrantAccessCommand command = grantAccessMapper.toCommand(message);
        AccessAssignmentResult result = commandBus.dispatch(command);
        return grantAccessMapper.toResponse(result);
    }

    public AssignmentMessageResult changeAssignmentStatus(ChangeAssignmentStatusMessage message) {
        ChangeAccessStatusCommand command = changeAccessStatusMapper.toCommand(message);
        AccessAssignmentResult result = commandBus.dispatch(command);
        return changeAccessStatusMapper.toResponse(result);
    }

    public AuthTokenResult switchAccessContext(SwitchContextMessage message) {
        SwitchAccessContextCommand command = switchContextMapper.toCommand(message);
        AuthResult result = commandBus.dispatch(command);
        return switchContextMapper.toResponse(result);
    }

    public AccessWriteAcknowledgement replaceAccess(ReplaceAccessMessage message) {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                message.userId(),
                message.previousRoleCode(),
                message.replacementRoleCode(),
                message.scopeKey(),
                message.scopeId(),
                message.authorityCodes() != null ? message.authorityCodes() : Set.of()
        );
        commandBus.dispatch(command);
        return new AccessWriteAcknowledgement();
    }

    public AccessWriteAcknowledgement revokeAccess(RevokeAccessMessage message) {
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                message.userId(),
                message.roleCode(),
                null,
                message.scopeKey(),
                message.scopeId(),
                Set.of()
        );
        commandBus.dispatch(command);
        return new AccessWriteAcknowledgement();
    }

    public AccessWriteAcknowledgement revokeSessionsByScope(RevokeSessionsByScopeMessage message) {
        RevokeSessionsByScopeCommand command = new RevokeSessionsByScopeCommand(
                message.scopeKey(),
                message.scopeId()
        );
        commandBus.dispatch(command);
        return new AccessWriteAcknowledgement();
    }
}
