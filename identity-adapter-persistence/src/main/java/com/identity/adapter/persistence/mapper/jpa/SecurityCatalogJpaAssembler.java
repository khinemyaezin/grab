package com.identity.adapter.persistence.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.ScopeDeclaration;
import com.grab.framework.security.ScopeDeclaration.Lifecycle;
import com.identity.adapter.persistence.entity.AuthorityEntity;
import com.identity.adapter.persistence.entity.ScopeManifestEntity;
import com.identity.adapter.persistence.entity.SecurityCatalogStateEntity;
import com.identity.adapter.persistence.entity.SecurityManifestModuleEntity;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.security.CatalogAuthority;
import com.identity.domain.security.CatalogModule;
import com.identity.domain.security.CatalogScope;

import java.util.List;

public class SecurityCatalogJpaAssembler {
    private final IdGenerator ids;

    public SecurityCatalogJpaAssembler(IdGenerator ids) { this.ids = ids; }

    public SecurityCatalog rehydrate(SecurityCatalogStateEntity state, List<ScopeManifestEntity> scopes,
            List<AuthorityEntity> authorities, List<SecurityManifestModuleEntity> modules) {
        List<CatalogScope> scopeState = scopes.stream().map(this::scope).toList();
        List<CatalogAuthority> authorityState = authorities.stream().map(this::authority).toList();
        List<CatalogModule> moduleState = modules.stream().map(this::module).toList();
        var id = new CommonId("00000000-0000-0000-0000-000000000001");
        long revision = state.getCatalogRevision();
        return SecurityCatalog.rehydrate(id, revision, scopeState, authorityState, moduleState);
    }

    private CatalogScope scope(ScopeManifestEntity entity) {
        String providerLifecycle = entity.getProviderLifecycle();
        Lifecycle lifecycle = Lifecycle.valueOf(providerLifecycle);
        String key = entity.getScopeKey();
        String owner = entity.getModuleKey();
        String parent = entity.getParentScopeKey();
        boolean enabled = entity.isActive();
        int sourceRevision = entity.getSourceRevision();
        return new CatalogScope(key, owner, parent, lifecycle, enabled, sourceRevision);
    }

    private CatalogAuthority authority(AuthorityEntity entity) {
        String providerLifecycle = entity.getProviderLifecycle();
        Lifecycle lifecycle = Lifecycle.valueOf(providerLifecycle);
        String code = entity.getCode();
        String owner = entity.getOwnerKey();
        boolean enabled = entity.isActive();
        int sourceRevision = entity.getSourceRevision();
        return new CatalogAuthority(code, owner, lifecycle, enabled, sourceRevision);
    }

    private CatalogModule module(SecurityManifestModuleEntity entity) {
        String owner = entity.getModuleKey();
        int revision = entity.getAppliedRevision();
        String digest = entity.getAppliedDigest();
        return new CatalogModule(owner, revision, digest);
    }

    public void updateAuthority(AuthorityEntity entity, AuthorityDefinition declaration, String owner, int revision) {
        if (entity.getUuid() == null) {
            var id = ids.generateId();
            String uuid = id.getValue();
            String code = declaration.code();
            entity.setUuid(uuid);
            entity.setCode(code);
            entity.setOwnerKey(owner);
            entity.setActive(true);
        }
        String category = declaration.category();
        String name = declaration.name();
        String description = declaration.description();
        var lifecycle = declaration.lifecycle();
        String providerLifecycle = lifecycle.name();
        entity.setCategory(category);
        entity.setName(name);
        entity.setDescription(description);
        entity.setProviderLifecycle(providerLifecycle);
        entity.setSourceRevision(revision);
    }

    public void updateScope(ScopeManifestEntity entity, ScopeDeclaration declaration, String owner, int revision) {
        if (entity.getId() == null) {
            entity.setModuleKey(owner);
            String key = declaration.scopeKey();
            String parent = declaration.parentScopeKey();
            entity.setScopeKey(key);
            entity.setParentScopeKey(parent);
            entity.setActive(true);
        }
        entity.setManifestVersion(revision);
        entity.setSourceRevision(revision);
        var lifecycle = declaration.lifecycle();
        String providerLifecycle = lifecycle.name();
        entity.setProviderLifecycle(providerLifecycle);
    }
}
