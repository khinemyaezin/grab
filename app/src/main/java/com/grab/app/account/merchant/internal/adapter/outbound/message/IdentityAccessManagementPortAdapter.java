package com.grab.app.account.merchant.internal.adapter.outbound.message;

import com.grab.app.account.identity.port.message.AccessAssignmentPort;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.ReplaceAccessMessage;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.RevokeAccessMessage;
import com.grab.app.account.identity.port.message.AccessAssignmentPort.RevokeSessionsByScopeMessage;
import com.grab.app.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
public class IdentityAccessManagementPortAdapter implements IdentityAccessManagementPort {

    private final AccessAssignmentPort identityPort;
    private final IdGenerator idGenerator;

    public IdentityAccessManagementPortAdapter(
            AccessAssignmentPort identityPort,
            IdGenerator idGenerator
    ) {
        this.identityPort = identityPort;
        this.idGenerator = idGenerator;
    }

    @Override
    @MerchantTransactional(propagation = Propagation.NOT_SUPPORTED)
    public void replaceAccess(ReplaceAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        ReplaceAccessMessage message = new ReplaceAccessMessage(
                userId,
                request.previousRoleCode(),
                request.roleCode(),
                request.scopeKey(),
                request.scopeId(),
                request.authorityCodes()
        );
        identityPort.replaceAccess(message);
    }

    @Override
    @MerchantTransactional(propagation = Propagation.NOT_SUPPORTED)
    public void revokeAccess(RevokeAccessRequest request) {
        Id userId = idGenerator.convertIdFrom(request.userId());
        RevokeAccessMessage message = new RevokeAccessMessage(
                userId,
                request.roleCode(),
                request.scopeKey(),
                request.scopeId()
        );
        identityPort.revokeAccess(message);
    }

    @Override
    @MerchantTransactional(propagation = Propagation.NOT_SUPPORTED)
    public void revokeSessionsByScope(String scopeKey, String scopeId) {
        RevokeSessionsByScopeMessage message = new RevokeSessionsByScopeMessage(scopeKey, scopeId);
        identityPort.revokeSessionsByScope(message);
    }
}
