package com.identity.domain.policy;

import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RolePermissionReference;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.security.CatalogAuthority;
import com.identity.domain.security.CatalogModule;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ProtectedRoleReconciliationPolicy {

    private ProtectedRoleReconciliationPolicy() {
    }

    public enum ReconciliationStatus {
        RECONCILED,
        WAITING_DEPENDENCY
    }

    public record ReconciliationOutcome(
            ReconciliationStatus status,
            Set<Authority> authorities,
            String reason
    ) {
        public boolean isReconciled() {
            return status == ReconciliationStatus.RECONCILED;
        }
    }

    public static ReconciliationOutcome evaluate(
            RoleDeclaration declaration,
            SecurityCatalog catalog,
            AuthorityRepository authorityRepository
    ) {
        try {
            catalog.requireEffectiveScope(declaration.assignmentScopeKey());
        } catch (Exception e) {
            return new ReconciliationOutcome(ReconciliationStatus.WAITING_DEPENDENCY, Set.of(),
                    "Scope is not effective yet: " + declaration.assignmentScopeKey());
        }

        for (Map.Entry<String, Integer> entry : declaration.minimumOwnerRevisions().entrySet()) {
            String moduleKey = entry.getKey();
            int minRev = entry.getValue();
            Optional<CatalogModule> mod = catalog.findModule(moduleKey);
            if (mod.isEmpty() || mod.get().appliedRevision() < minRev) {
                return new ReconciliationOutcome(ReconciliationStatus.WAITING_DEPENDENCY, Set.of(),
                        "Waiting for module manifest: " + moduleKey + " minRevision=" + minRev);
            }
        }

        Set<String> authorityCodes = new HashSet<>();
        for (RolePermissionReference ref : declaration.permissions()) {
            Optional<CatalogAuthority> catalogAuth = catalog.findAuthority(ref.code());
            if (catalogAuth.isEmpty()) {
                return new ReconciliationOutcome(ReconciliationStatus.WAITING_DEPENDENCY, Set.of(),
                        "Authority not yet registered: " + ref.code());
            }
            CatalogAuthority auth = catalogAuth.get();
            if (!ref.owner().equalsIgnoreCase(auth.owner())) {
                throw new IdentityDomainValidationException(
                        new IdentityDomainError.InvalidAuthorityCode(ref.code()),
                        "Authority owner mismatch for " + ref.code() + ": expected " + ref.owner() + " but catalog reports " + auth.owner()
                );
            }
            if (!auth.isEffective()) {
                return new ReconciliationOutcome(ReconciliationStatus.WAITING_DEPENDENCY, Set.of(),
                        "Authority is not effective: " + ref.code());
            }
            authorityCodes.add(ref.code());
        }

        Set<Authority> activeAuthorities = authorityRepository.findActiveByCodes(authorityCodes);
        if (activeAuthorities.size() != authorityCodes.size()) {
            return new ReconciliationOutcome(ReconciliationStatus.WAITING_DEPENDENCY, Set.of(),
                    "Not all active Authority entities exist in repository yet");
        }

        return new ReconciliationOutcome(ReconciliationStatus.RECONCILED, activeAuthorities, null);
    }
}
