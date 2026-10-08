package com.identity.application.model.read;

import com.identity.domain.valueobject.ScopeHierarchy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public record ScopeCatalogView(Map<String, String> parents, Map<String, String> owners, Set<String> inactive) {
    public ScopeCatalogView {
        parents = Collections.unmodifiableMap(new LinkedHashMap<>(parents));
        owners = Map.copyOf(owners);
        inactive = Set.copyOf(inactive);
    }

    public ScopeHierarchy hierarchy() {
        return new ScopeHierarchy(parents, owners, inactive);
    }
}
