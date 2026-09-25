package com.grab.store.identity.port;

import java.util.Set;

public interface AccessManagementPort {
    void replaceAccess(ReplaceAccessRequest request);
    void revokeAccess(RevokeAccessRequest request);
    void revokeSessionsByScope(String scopeKey, String scopeId);

    record ReplaceAccessRequest(
            String userId,
            String previousRoleCode,
            String roleCode,
            String scopeKey,
            String scopeId,
            Set<String> authorityCodes
    ) {
        public ReplaceAccessRequest(
                String userId,
                String previousRoleCode,
                String roleCode,
                String scopeKey,
                String scopeId
        ) {
            this(userId, previousRoleCode, roleCode, scopeKey, scopeId, Set.of());
        }
    }

    record RevokeAccessRequest(
            String userId,
            String roleCode,
            String scopeKey,
            String scopeId
    ) {
        public RevokeAccessRequest(
                String userId,
                String scopeKey,
                String scopeId
        ) {
            this(userId, null, scopeKey, scopeId);
        }
    }
}
