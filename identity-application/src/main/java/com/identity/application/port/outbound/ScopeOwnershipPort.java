package com.identity.application.port.outbound;

public interface ScopeOwnershipPort {
    boolean isResourceOwnedByScope(
            String actorScopeKey,
            String actorScopeId,
            String targetScopeKey,
            String targetScopeId
    );
}
