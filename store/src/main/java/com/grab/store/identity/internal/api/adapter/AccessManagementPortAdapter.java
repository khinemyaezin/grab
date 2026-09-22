package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.internal.config.IdentityTransactional;
import com.grab.store.identity.port.AccessManagementPort;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import com.identity.application.port.inbound.RevokeSessionsByScopeUseCase;
import lombok.RequiredArgsConstructor;
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
    @IdentityTransactional
    public void grantAccess(GrantAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        var command = new ReplaceAccessCommand(
                userId,
                request.platformCode(),
                request.roleCode(),
                request.scopeKey(),
                request.scopeId()
        );
        replaceAccessUseCase.execute(command);
    }

    @Override
    @IdentityTransactional
    public void revokeAccess(RevokeAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        var command = new ReplaceAccessCommand(
                userId,
                request.platformCode(),
                null,
                request.scopeKey(),
                request.scopeId()
        );
        replaceAccessUseCase.execute(command);
    }

    @Override
    @IdentityTransactional
    public void revokeSessionsByScope(String platformCode, String scopeKey, String scopeId) {
        var command = new RevokeSessionsByScopeCommand(
                platformCode,
                scopeKey,
                scopeId
        );
        revokeSessionsByScopeUseCase.execute(command);
    }
}
