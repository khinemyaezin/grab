package com.identity.domain.security;

import com.grab.framework.security.ScopeDeclaration.Lifecycle;

public record CatalogScope(String key, String owner, String parent, Lifecycle lifecycle, boolean locallyEnabled, int sourceRevision) {
    public boolean isEffective() {
        return lifecycle == Lifecycle.ACTIVE && locallyEnabled && sourceRevision > 0 && owner != null && !owner.isBlank();
    }
}
