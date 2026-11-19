package com.identity.application.model.read;

import com.grab.framework.id.Id;
import java.util.Set;

public record SellerAccessResult(Status status, Id userId, String email, long authenticationVersion,
                                 Id assignmentId, String scopeKey, Id scopeId, Set<String> roles,
                                 Set<String> authorities, long merchantRevision, long storefrontRevision) {
    public enum Status { ACTIVE, UNKNOWN_IDENTITY, INACTIVE_IDENTITY, INVALID_CONTEXT, DENIED }

    public SellerAccessResult {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
    }
}
