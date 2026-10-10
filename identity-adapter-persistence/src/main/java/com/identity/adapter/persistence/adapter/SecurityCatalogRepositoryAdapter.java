package com.identity.adapter.persistence.adapter;

import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.adapter.persistence.entity.*;
import com.identity.adapter.persistence.repository.jpa.*;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.adapter.persistence.mapper.jpa.SecurityCatalogJpaAssembler;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.adapter.persistence.exception.IdentityInfraError;
import com.identity.adapter.persistence.exception.IdentityInfraException;
import lombok.AllArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

@AllArgsConstructor
public class SecurityCatalogRepositoryAdapter implements SecurityCatalogRepository {
    private final SecurityCatalogStateJpaRepository states;
    private final SecurityManifestModuleJpaRepository modules;
    private final ScopeManifestJpaRepository scopes;
    private final AuthorityJpaRepository authorities;
    private final SecurityCatalogJpaAssembler assembler;
    private final DomainEventProducer outbox;
    private final PersistenceExecutor executor;

    @Override
    public boolean ensureInitialized(boolean creationAllowed) {
        try {
            return executor.command("SecurityCatalogInitialization", () -> {
                if (states.existsById(1L)) {
                    return true;
                }
                if (!creationAllowed) {
                    return false;
                }

                var initial = new SecurityCatalogStateEntity();
                initial.setId(1L);
                initial.setCatalogRevision(0L);
                states.saveAndFlush(initial);
                return true;
            });
        } catch (IdentityInfraException exception) {
            String constraintName = catalogStatePrimaryKeyConstraint(exception);
            if (constraintName == null) {
                throw exception;
            }
            throw new IdentityInfraException(
                    new IdentityInfraError.SecurityCatalogInitializationRace(constraintName),
                    "Security catalog initialization raced with another writer.",
                    exception
            );
        }
    }

    @Override
    public SecurityCatalog loadForUpdate() {
        return executor.command("SecurityCatalog", () -> {
            var state = states.lockSingleton();
            if (state == null) {
                throw new IdentityInfraException(
                        new IdentityInfraError.SecurityCatalogStateMissing(),
                        "Security catalog state is missing after initialization.",
                        null
                );
            }
            var scopeState = scopes.findAll();
            var authorityState = authorities.findAll();
            var moduleState = modules.findAll();
            return assembler.rehydrate(state, scopeState, authorityState, moduleState);
        });
    }

    private String catalogStatePrimaryKeyConstraint(Throwable failure) {
        Throwable cause = failure;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();
                if ("security_catalog_state_pkey".equals(constraintName)) {
                    return constraintName;
                }
            }
            cause = cause.getCause();
        }
        return null;
    }

    @Override
    public void save(SecurityCatalog catalog) {
        executor.command("SecurityCatalog", () -> {
            var activation = catalog.activatedManifest();
            var manifest = activation.orElseThrow();
            String owner = manifest.moduleKey();
            int revision = manifest.securityRevision();
            String digest = manifest.contentDigest();
            for (var declaration : manifest.authorities()) {
                String code = declaration.code();
                var existing = authorities.findByCode(code);
                AuthorityEntity entity = existing.orElseGet(AuthorityEntity::new);
                assembler.updateAuthority(entity, declaration, owner, revision);
                authorities.save(entity);
            }
            for (var declaration : manifest.scopes()) {
                String key = declaration.scopeKey();
                var existing = scopes.findByScopeKey(key);
                ScopeManifestEntity entity = existing.orElseGet(ScopeManifestEntity::new);
                assembler.updateScope(entity, declaration, owner, revision);
                scopes.save(entity);
            }
            var existingModule = modules.findById(owner);
            var module = existingModule.orElseGet(SecurityManifestModuleEntity::new);
            module.setModuleKey(owner);
            module.setAppliedRevision(revision);
            module.setAppliedDigest(digest);
            modules.save(module);
            var state = states.lockSingleton();
            long catalogRevision = catalog.revision();
            state.setCatalogRevision(catalogRevision);
            states.save(state);
            var events = catalog.pullEvents();
            var catalogId = catalog.getId();
            String aggregateId = catalogId.getValue();
            outbox.produce("SecurityCatalog", aggregateId, events);
            return null;
        });
    }
}
