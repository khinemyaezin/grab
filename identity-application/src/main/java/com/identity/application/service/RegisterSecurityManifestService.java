package com.identity.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.port.inbound.RegisterSecurityManifestUseCase;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.ScopeManifestRepository;
import com.identity.domain.port.outbound.SecurityCatalogLock;
import com.identity.domain.port.outbound.SecurityManifestCatalogRepository;
import com.identity.domain.port.outbound.SecurityManifestInboxRepository;
import com.identity.domain.port.outbound.SecurityManifestModuleRepository;
import com.identity.domain.port.outbound.SecurityManifestRevisionRepository;
import com.identity.domain.security.SecurityManifestCandidateStatus;
import com.identity.domain.security.SecurityManifestValidator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegisterSecurityManifestService implements RegisterSecurityManifestUseCase {
    private static final Logger log = Loggers.getLogger(RegisterSecurityManifestService.class);

    private final SecurityManifestInboxRepository inbox;
    private final ScopeManifestRepository scopeManifest;
    private final SecurityCatalogLock catalogLock;
    private final SecurityManifestRevisionRepository revisions;
    private final AuthorityRepository authorityCatalog;
    private final SecurityManifestModuleRepository modules;
    private final SecurityManifestCatalogRepository catalog;

    public RegisterSecurityManifestService(
            SecurityManifestInboxRepository inbox,
            SecurityManifestCatalogRepository catalog
    ) {
        this(inbox, null, null, null, null, null, catalog);
    }

    public RegisterSecurityManifestService(
            SecurityManifestInboxRepository inbox,
            ScopeManifestRepository scopeManifest,
            SecurityCatalogLock catalogLock,
            SecurityManifestRevisionRepository revisions,
            AuthorityRepository authorityCatalog,
            SecurityManifestModuleRepository modules,
            SecurityManifestCatalogRepository catalog
    ) {
        this.inbox = inbox;
        this.scopeManifest = scopeManifest;
        this.catalogLock = catalogLock;
        this.revisions = revisions;
        this.authorityCatalog = authorityCatalog;
        this.modules = modules;
        this.catalog = catalog;
    }

    @Override
    public void execute(RegisterSecurityManifestCommand command) {
        var manifest = command.manifest();
        if (catalogLock != null) {
            catalogLock.lockNowait();
        }
        if (!manifest.contentDigest().equals(command.suppliedContentDigest())) {
            var error = new IdentityDomainError.SecurityManifestDigestMismatch(
                    command.suppliedContentDigest(), manifest.contentDigest());
            record(command, SecurityManifestCandidateStatus.QUARANTINED, error.code());
            return;
        }
        if (inbox != null) {
            var existingEvent = inbox.find(command.eventId());
            if (existingEvent.isEmpty() && inbox.alreadyProcessed(command.eventId())) {
                return;
            }
            if (existingEvent.isPresent()) {
                var receipt = existingEvent.get();
                if (!receipt.contentDigest().equals(manifest.contentDigest())) {
                    var error = new IdentityDomainError.SecurityManifestPayloadConflict(command.eventId());
                    record(command, SecurityManifestCandidateStatus.QUARANTINED, error.code());
                    return;
                }
                if (receipt.status() != SecurityManifestCandidateStatus.WAITING_DEPENDENCY) {
                    return;
                }
            }
            var existingRevision = inbox.findByModuleRevision(manifest.moduleKey(), manifest.securityRevision());
            if (existingRevision.isPresent() && !existingRevision.get().contentDigest().equals(manifest.contentDigest())) {
                var error = new IdentityDomainError.SecurityManifestRevisionConflict(manifest.moduleKey(), manifest.securityRevision());
                record(command, SecurityManifestCandidateStatus.QUARANTINED, error.code());
                return;
            }
            int appliedRevision = modules == null
                    ? inbox.appliedRevision(manifest.moduleKey())
                    : modules.appliedRevision(manifest.moduleKey());
            if (manifest.securityRevision() < appliedRevision) {
                var error = new IdentityDomainError.SecurityManifestStaleRevision(
                        manifest.moduleKey(), manifest.securityRevision(), appliedRevision);
                record(command, SecurityManifestCandidateStatus.SUPERSEDED, error.code());
                return;
            }
            if (revisions != null && manifest.securityRevision() < revisions.highestAcceptedRevision(manifest.moduleKey())) {
                int highestAccepted = revisions.highestAcceptedRevision(manifest.moduleKey());
                var error = new IdentityDomainError.SecurityManifestLowerThanPending(
                        manifest.moduleKey(), manifest.securityRevision(), highestAccepted);
                record(command, SecurityManifestCandidateStatus.SUPERSEDED, error.code());
                return;
            }
            Map<String, String> knownOwners = scopeManifest == null ? Map.of() : scopeManifest.loadOwners();
            for (var dependency : manifest.dependencies()) {
                String dependencyOwner = knownOwners.get(dependency.scopeKey());
                if (dependencyOwner == null) {
                    var error = new IdentityDomainError.SecurityManifestMissingDependency(dependency.scopeKey());
                    record(command, SecurityManifestCandidateStatus.WAITING_DEPENDENCY, error.code());
                    return;
                }
                int dependencyRevision = modules == null
                        ? inbox.appliedRevision(dependencyOwner)
                        : modules.appliedRevision(dependencyOwner);
                if (dependencyRevision < dependency.minimumRevision()) {
                    var error = new IdentityDomainError.SecurityManifestMissingDependency(dependency.scopeKey());
                    record(command, SecurityManifestCandidateStatus.WAITING_DEPENDENCY, error.code());
                    return;
                }
            }
            Map<String, String> knownScopes = scopeManifest == null ? Map.of() : scopeManifest.loadGraph();
            Set<String> foreignScopes = new HashSet<>();
            for (Map.Entry<String, String> entry : knownOwners.entrySet()) {
                if (!manifest.moduleKey().equals(entry.getValue())) {
                    foreignScopes.add(entry.getKey());
                }
            }
            if (authorityCatalog != null) {
                var previousCodes = authorityCatalog.findCodesByModule(manifest.moduleKey());
                Set<String> declaredCodes = new HashSet<>();
                for (var definition : manifest.authorities()) {
                    declaredCodes.add(definition.code());
                }
                if (!declaredCodes.containsAll(previousCodes)) {
                    var error = new IdentityDomainError.SecurityManifestOmittedAuthority(manifest.moduleKey());
                    record(command, SecurityManifestCandidateStatus.QUARANTINED, error.code());
                    return;
                }
            }
            Set<String> ownedByOthers = authorityCatalog == null
                    ? Set.of()
                    : authorityCatalog.findCodesOwnedByOtherModules(manifest.moduleKey());
            var validationError = SecurityManifestValidator.validate(manifest, knownScopes, foreignScopes, ownedByOthers);
            if (validationError.isPresent()) {
                record(command, SecurityManifestCandidateStatus.QUARANTINED, validationError.get().code());
                return;
            }
        }
        if (catalog != null) {
            catalog.apply(manifest);
        }
        if (modules != null) {
            modules.recordApplied(manifest.moduleKey(), manifest.securityRevision(), manifest.contentDigest());
        }
        if (catalogLock != null) {
            catalogLock.recordActivation();
        }
        if (inbox != null) {
            record(command, SecurityManifestCandidateStatus.APPLIED, null);
        }
    }

    private void record(RegisterSecurityManifestCommand command, SecurityManifestCandidateStatus status, String errorCode) {
        var manifest = command.manifest();
        log.info("Security manifest module: {}, revision: {}, outcome: {}, errorCode: {}",
                manifest.moduleKey(), manifest.securityRevision(), status, errorCode);
        if (revisions != null) {
            revisions.record(command.eventId(), manifest, status, errorCode);
        }
        if (inbox != null) {
            inbox.recordOutcome(command.eventId(), manifest.moduleKey(), manifest.securityRevision(),
                    manifest.contentDigest(), status, errorCode);
        }
    }
}
