package com.grab.service.account.merchant.internal.adapter.outbound.message;

import com.grab.service.contract.message.account.identity.AccessAssignmentPort;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.ReplaceAccessMessage;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.RevokeAccessMessage;
import com.grab.service.contract.message.account.identity.AccessAssignmentPort.RevokeSessionsByScopeMessage;
import com.grab.service.account.merchant.internal.config.MerchantTransactional;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import java.rmi.RemoteException;

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
        try {
            identityPort.replaceAccess(message);
        } catch (RemoteException exception) {
            throw new IllegalStateException("Unable to apply the merchant identity access change", exception);
        }
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
        try {
            identityPort.revokeAccess(message);
        } catch (RemoteException exception) {
            throw new IllegalStateException("Unable to apply the merchant identity access change", exception);
        }
    }

    @Override
    @MerchantTransactional(propagation = Propagation.NOT_SUPPORTED)
    public void revokeSessionsByScope(String scopeKey, String scopeId) {
        RevokeSessionsByScopeMessage message = new RevokeSessionsByScopeMessage(scopeKey, scopeId);
        try {
            identityPort.revokeSessionsByScope(message);
        } catch (RemoteException exception) {
            throw new IllegalStateException("Unable to apply the merchant identity access change", exception);
        }
    }
}
