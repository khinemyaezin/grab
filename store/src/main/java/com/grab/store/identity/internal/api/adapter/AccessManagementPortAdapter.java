package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.internal.config.RequiresSecurityCatalog;
import com.grab.store.identity.port.AccessManagementPort;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import com.identity.application.port.inbound.RevokeSessionsByScopeUseCase;
import org.springframework.stereotype.Component;

@Component
public class AccessManagementPortAdapter implements AccessManagementPort {

    private final ReplaceAccessUseCase replaceAccessUseCase;
    private final RevokeSessionsByScopeUseCase revokeSessionsByScopeUseCase;
    private final IdGenerator idGenerator;

    public AccessManagementPortAdapter(
            ReplaceAccessUseCase replaceAccessUseCase,
            RevokeSessionsByScopeUseCase revokeSessionsByScopeUseCase,
            IdGenerator idGenerator
    ) {
        this.replaceAccessUseCase = replaceAccessUseCase;
        this.revokeSessionsByScopeUseCase = revokeSessionsByScopeUseCase;
        this.idGenerator = idGenerator;
    }

    @Override
    @RequiresSecurityCatalog
    @IdentityTransactional
    public void replaceAccess(ReplaceAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        var command = new ReplaceAccessCommand(
                userId,
                request.previousRoleCode(),
                request.roleCode(),
                request.scopeKey(),
                request.scopeId(),
                request.authorityCodes()
        );
        replaceAccessUseCase.execute(command);
    }

    @Override
    @IdentityTransactional
    public void revokeAccess(RevokeAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        var command = new ReplaceAccessCommand(
                userId,
                request.roleCode(),
                null,
                request.scopeKey(),
                request.scopeId()
        );
        replaceAccessUseCase.execute(command);
    }

    @Override
    @IdentityTransactional
    public void revokeSessionsByScope(String scopeKey, String scopeId) {
        var command = new RevokeSessionsByScopeCommand(
                scopeKey,
                scopeId
        );
        revokeSessionsByScopeUseCase.execute(command);
    }
}
