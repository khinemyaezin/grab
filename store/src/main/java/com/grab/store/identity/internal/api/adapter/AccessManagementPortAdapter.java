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

import java.util.Set;

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
        String userIdString = request.userId();
        Id userId = idGenerator.convertIdFrom(userIdString);
        String previousRoleCode = request.previousRoleCode();
        String roleCode = request.roleCode();
        String scopeKey = request.scopeKey();
        String scopeId = request.scopeId();
        Set<String> authorityCodes = request.authorityCodes();
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId,
                previousRoleCode,
                roleCode,
                scopeKey,
                scopeId,
                authorityCodes
        );
        replaceAccessUseCase.execute(command);
    }

    @Override
    @IdentityTransactional
    public void revokeAccess(RevokeAccessRequest request) {
        String userIdString = request.userId();
        Id userId = idGenerator.convertIdFrom(userIdString);
        String roleCode = request.roleCode();
        String scopeKey = request.scopeKey();
        String scopeId = request.scopeId();
        ReplaceAccessCommand command = new ReplaceAccessCommand(
                userId,
                roleCode,
                null,
                scopeKey,
                scopeId
        );
        replaceAccessUseCase.execute(command);
    }

    @Override
    @IdentityTransactional
    public void revokeSessionsByScope(String scopeKey, String scopeId) {
        RevokeSessionsByScopeCommand command = new RevokeSessionsByScopeCommand(
                scopeKey,
                scopeId
        );
        revokeSessionsByScopeUseCase.execute(command);
    }
}
