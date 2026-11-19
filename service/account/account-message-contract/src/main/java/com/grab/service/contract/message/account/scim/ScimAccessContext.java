package com.grab.service.contract.message.account.scim;

import java.io.Serializable;
import java.util.Set;

public record ScimAccessContext(
        String assignmentId,
        String scopeKey,
        String scopeId,
        String role,
        String displayName,
        Set<String> authorities
) implements Serializable {
    public ScimAccessContext {
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
    }
}
