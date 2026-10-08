package com.identity.adapter.persistence.adapter;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.SecurityManifest;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.ScopeManifestRepository;
import com.identity.domain.port.outbound.SecurityManifestCatalogRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SecurityManifestCatalogRepositoryAdapter implements SecurityManifestCatalogRepository {
    private final AuthorityRepository authorities;
    private final ScopeManifestRepository scopes;
    private final IdGenerator ids;

    @Override
    public void apply(SecurityManifest manifest) {
        List<Authority> authorityList = manifest.authorities().stream()
                .map(definition -> Authority.from(ids.generateId(), manifest.moduleKey(), definition))
                .toList();
        authorities.upsertAll(authorityList);

        boolean applied = scopes.apply(manifest.moduleKey(), manifest.securityRevision(), manifest.scopes());
        if (!applied) {
            throw new IllegalStateException("security scope manifest could not be applied atomically");
        }

        Set<String> retiredCodes = manifest.authorities().stream()
                .filter(definition -> definition.lifecycle() == ScopeDeclaration.Lifecycle.RETIRED)
                .map(AuthorityDefinition::code)
                .collect(Collectors.toSet());
        authorities.retireCodes(manifest.moduleKey(), retiredCodes);
    }
}
