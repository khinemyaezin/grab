package com.grab.app.account.merchant.internal.adapter;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class IdentityAccessManagementPortAdapter implements IdentityAccessManagementPort {

    private final CommandBus accountCommandBus;
    private final IdGenerator idGenerator;

    public IdentityAccessManagementPortAdapter(
            @Qualifier("accountCommandBus") CommandBus accountCommandBus,
            IdGenerator idGenerator
    ) {
        this.accountCommandBus = accountCommandBus;
        this.idGenerator = idGenerator;
    }

    @Override
    public void replaceAccess(ReplaceAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId,
                request.previousRoleCode(),
                request.roleCode(),
                request.scopeKey(),
                request.scopeId(),
                request.authorityCodes() != null ? request.authorityCodes() : Set.of()
        );
        accountCommandBus.dispatch(command);
    }

    @Override
    public void revokeAccess(RevokeAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId,
                request.roleCode(),
                null,
                request.scopeKey(),
                request.scopeId(),
                Set.of()
        );
        accountCommandBus.dispatch(command);
    }

    @Override
    public void revokeSessionsByScope(String scopeKey, String scopeId) {
        RevokeSessionsByScopeCommand command = new RevokeSessionsByScopeCommand(scopeKey, scopeId);
        accountCommandBus.dispatch(command);
    }
}

