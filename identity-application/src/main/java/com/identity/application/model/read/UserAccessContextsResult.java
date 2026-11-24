package com.identity.application.model.read;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Set;

public record UserAccessContextsResult(
        Id userId,
        List<AccessContextView> contexts
) {
    public UserAccessContextsResult {
        contexts = contexts == null ? List.of() : List.copyOf(contexts);
    }

    public record AccessContextView(
            String assignmentId,
            String scopeKey,
            String scopeId,
            String role,
            String displayName,
            Set<String> authorities
    ) {
        public AccessContextView {
            authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
        }
    }
}
