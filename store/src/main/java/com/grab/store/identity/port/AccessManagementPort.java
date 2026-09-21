package com.grab.store.identity.port;

public interface AccessManagementPort {
    void grantAccess(GrantAccessRequest request);
    void revokeAccess(RevokeAccessRequest request);
    void revokeSessionsByScope(String platformCode, String scopeKey, String scopeId);

    record GrantAccessRequest(
            String userId,
            String platformCode,
            String roleCode,
            String scopeKey,
            String scopeId
    ) {}

    record RevokeAccessRequest(
            String userId,
            String platformCode,
            String scopeKey,
            String scopeId
    ) {}
}
