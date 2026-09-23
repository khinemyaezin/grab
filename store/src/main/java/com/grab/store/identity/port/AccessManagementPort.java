package com.grab.store.identity.port;

public interface AccessManagementPort {
    void replaceAccess(ReplaceAccessRequest request);
    void revokeAccess(RevokeAccessRequest request);
    void revokeSessionsByScope(String platformCode, String scopeKey, String scopeId);

    record ReplaceAccessRequest(
            String userId,
            String platformCode,
            String previousRoleCode,
            String roleCode,
            String scopeKey,
            String scopeId
    ) {
    }

    record RevokeAccessRequest(
            String userId,
            String platformCode,
            String roleCode,
            String scopeKey,
            String scopeId
    ) {
        public RevokeAccessRequest(
                String userId,
                String platformCode,
                String scopeKey,
                String scopeId
        ) {
            this(userId, platformCode, null, scopeKey, scopeId);
        }
    }
}
