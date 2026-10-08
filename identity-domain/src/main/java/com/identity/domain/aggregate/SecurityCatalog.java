package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.grab.framework.security.SecurityManifest;
import com.identity.domain.event.SecurityCatalogActivatedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.policy.SecurityManifestRegistrationPolicy;
import com.identity.domain.security.*;
import com.identity.domain.valueobject.ScopeHierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SecurityCatalog extends AggregateRoot<Id> {
    private long revision;
    private final Map<String, CatalogScope> scopes;
    private final Map<String, CatalogAuthority> authorities;
    private final Map<String, CatalogModule> modules;
    private SecurityManifest activatedManifest;

    private SecurityCatalog(Id id, long revision, List<CatalogScope> scopes,
                            List<CatalogAuthority> authorities, List<CatalogModule> modules) {
        super(id);
        this.revision = revision;
        this.scopes = new HashMap<>();
        this.authorities = new HashMap<>();
        this.modules = new HashMap<>();
        scopes.forEach(scope -> this.scopes.put(scope.key(), scope));
        authorities.forEach(authority -> this.authorities.put(authority.code(), authority));
        modules.forEach(module -> this.modules.put(module.moduleKey(), module));
    }

    public static SecurityCatalog rehydrate(Id id, long revision, List<CatalogScope> scopes,
            List<CatalogAuthority> authorities, List<CatalogModule> modules) {
        return new SecurityCatalog(id, revision, scopes, authorities, modules);
    }

    public long revision() { return revision; }
    public Optional<SecurityManifest> activatedManifest() { return Optional.ofNullable(activatedManifest); }

    public SecurityManifestDecision consider(SecurityManifest manifest, String suppliedDigest, String eventId,
            Optional<SecurityManifestReceipt> receipt, Optional<SecurityManifestCandidate> canonical, int highestAccepted) {
        String digest = manifest.contentDigest();
        if (!digest.equals(suppliedDigest)) {
            var error = new IdentityDomainError.SecurityManifestDigestMismatch(suppliedDigest, digest);
            return conflict(error);
        }
        if (receipt.isPresent()) {
            var existing = receipt.get();
            if (!digest.equals(existing.contentDigest())) {
                var error = new IdentityDomainError.SecurityManifestPayloadConflict(eventId);
                return conflict(error);
            }
        }
        if (canonical.isPresent() && !digest.equals(canonical.get().manifest().contentDigest())) {
            var error = new IdentityDomainError.SecurityManifestRevisionConflict(manifest.moduleKey(), manifest.securityRevision());
            return conflict(error);
        }
        CatalogModule module = modules.get(manifest.moduleKey());
        int applied = module == null ? 0 : module.appliedRevision();
        if (manifest.securityRevision() == applied && !digest.equals(module.appliedDigest())) {
            var error = new IdentityDomainError.SecurityManifestRevisionConflict(manifest.moduleKey(), manifest.securityRevision());
            return conflict(error);
        }
        if (receipt.isPresent() && receipt.get().status() != SecurityManifestCandidateStatus.WAITING_DEPENDENCY) {
            return new SecurityManifestDecision(receipt.get().status(), null, false, false);
        }
        if (manifest.securityRevision() == applied) {
            return new SecurityManifestDecision(SecurityManifestCandidateStatus.APPLIED, null, false, false);
        }
        if (canonical.isPresent() && (canonical.get().status() == SecurityManifestCandidateStatus.QUARANTINED
                || canonical.get().status() == SecurityManifestCandidateStatus.SUPERSEDED)) {
            return new SecurityManifestDecision(canonical.get().status(), null, false, false);
        }
        if (manifest.securityRevision() < applied) {
            var error = new IdentityDomainError.SecurityManifestStaleRevision(manifest.moduleKey(), manifest.securityRevision(), applied);
            return new SecurityManifestDecision(SecurityManifestCandidateStatus.SUPERSEDED, error, false, false);
        }
        if (manifest.securityRevision() < highestAccepted) {
            var error = new IdentityDomainError.SecurityManifestLowerThanPending(manifest.moduleKey(), manifest.securityRevision(), highestAccepted);
            return new SecurityManifestDecision(SecurityManifestCandidateStatus.SUPERSEDED, error, false, false);
        }
        SecurityManifestDecision decision = SecurityManifestRegistrationPolicy.evaluate(manifest, scopes, authorities, modules);
        if (decision.newlyActivated()) {
            activate(manifest);
        }
        return decision;
    }

    private void activate(SecurityManifest manifest) {
        String owner = manifest.moduleKey();
        int sourceRevision = manifest.securityRevision();
        String digest = manifest.contentDigest();
        for (var declaration : manifest.scopes()) {
            String key = declaration.scopeKey();
            String parent = declaration.parentScopeKey();
            var lifecycle = declaration.lifecycle();
            CatalogScope existing = scopes.get(key);
            boolean enabled = existing == null || existing.locallyEnabled();
            var scope = new CatalogScope(key, owner, parent, lifecycle, enabled, sourceRevision);
            scopes.put(key, scope);
        }
        for (var declaration : manifest.authorities()) {
            String code = declaration.code();
            var lifecycle = declaration.lifecycle();
            CatalogAuthority existing = authorities.get(code);
            boolean enabled = existing == null || existing.locallyEnabled();
            var authority = new CatalogAuthority(code, owner, lifecycle, enabled, sourceRevision);
            authorities.put(code, authority);
        }
        var module = new CatalogModule(owner, sourceRevision, digest);
        modules.put(owner, module);
        activatedManifest = manifest;
        revision++;
        var event = new SecurityCatalogActivatedEvent(revision, owner, sourceRevision, digest);
        addEvent(event);
    }

    public ScopeHierarchy hierarchy() {
        Map<String, String> parents = new HashMap<>();
        Map<String, String> owners = new HashMap<>();
        Set<String> inactive = new HashSet<>();
        for (CatalogScope scope : scopes.values()) {
            parents.put(scope.key(), scope.parent());
            owners.put(scope.key(), scope.owner());
            if (!scope.isEffective()) {
                inactive.add(scope.key());
            }
        }
        return new ScopeHierarchy(parents, owners, inactive);
    }

    public void requireEffectiveScope(String key) {
        if (!SecurityManifestRegistrationPolicy.isEffective(key, scopes)) {
            var error = new IdentityDomainError.InvalidScopeKey(key);
            throw new IdentityDomainValidationException(error, "Scope is unknown or ineffective");
        }
    }

    private SecurityManifestDecision conflict(IdentityDomainError error) {
        return new SecurityManifestDecision(SecurityManifestCandidateStatus.QUARANTINED, error, false, true);
    }
}
