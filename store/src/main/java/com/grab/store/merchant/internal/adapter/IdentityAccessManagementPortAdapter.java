package com.grab.store.merchant.internal.adapter;

import com.grab.store.identity.port.AccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class IdentityAccessManagementPortAdapter implements IdentityAccessManagementPort {

    private final AccessManagementPort accessManagementPort;

    @Override
    public void replaceAccess(ReplaceAccessRequest request) {
        String userId = request.userId();
        String previousRoleCode = request.previousRoleCode();
        String roleCode = request.roleCode();
        String scopeKey = request.scopeKey();
        String scopeId = request.scopeId();
        Set<String> authorityCodes = request.authorityCodes();
        AccessManagementPort.ReplaceAccessRequest identityRequest = new AccessManagementPort.ReplaceAccessRequest(
                userId,
                previousRoleCode,
                roleCode,
                scopeKey,
                scopeId,
                authorityCodes
        );
        accessManagementPort.replaceAccess(identityRequest);
    }

    @Override
    public void revokeAccess(RevokeAccessRequest request) {
        String userId = request.userId();
        String roleCode = request.roleCode();
        String scopeKey = request.scopeKey();
        String scopeId = request.scopeId();
        AccessManagementPort.RevokeAccessRequest identityRequest = new AccessManagementPort.RevokeAccessRequest(
                userId,
                roleCode,
                scopeKey,
                scopeId
        );
        accessManagementPort.revokeAccess(identityRequest);
    }

    @Override
    public void revokeSessionsByScope(String scopeKey, String scopeId) {
        accessManagementPort.revokeSessionsByScope(scopeKey, scopeId);
    }
}
