package com.identity.adapter.persistence.adapter;

import com.identity.adapter.persistence.repository.jpa.ScopeManifestJpaRepository;
import com.identity.application.model.read.ScopeCatalogView;
import com.identity.application.port.outbound.ScopeCatalogQueryPort;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class ScopeCatalogQueryAdapter implements ScopeCatalogQueryPort {
    private final ScopeManifestJpaRepository scopes;

    @Override
    public ScopeCatalogView load() {
        Map<String, String> parents = new HashMap<>();
        Map<String, String> owners = new HashMap<>();
        Set<String> inactive = new HashSet<>();
        for (var scope : scopes.findAll()) {
            parents.put(scope.getScopeKey(), scope.getParentScopeKey());
            owners.put(scope.getScopeKey(), scope.getModuleKey());
            if (!scope.isEffective()) {
                inactive.add(scope.getScopeKey());
            }
        }
        return new ScopeCatalogView(parents, owners, inactive);
    }
}
