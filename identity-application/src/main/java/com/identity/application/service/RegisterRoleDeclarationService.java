package com.identity.application.service;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.role.RoleDeclaration;
import com.identity.application.model.write.RegisterRoleDeclarationCommand;
import com.identity.application.model.write.RegisterRoleDeclarationResult;
import com.identity.application.port.inbound.RegisterRoleDeclarationUseCase;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.aggregate.Role;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.policy.ProtectedRoleReconciliationPolicy;
import com.identity.domain.policy.ProtectedRoleReconciliationPolicy.ReconciliationOutcome;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.domain.port.outbound.RoleDeclarationRepository;
import com.identity.domain.port.outbound.RoleRepository;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.security.RoleDeclarationCandidate;
import com.identity.domain.security.RoleDeclarationCandidateStatus;
import com.identity.domain.security.RoleDeclarationReceipt;
import com.identity.domain.security.RoleDeclarationState;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

    public class RegisterRoleDeclarationService implements RegisterRoleDeclarationUseCase {
    private static final Logger log = Loggers.getLogger(RegisterRoleDeclarationService.class);
    private final SecurityCatalogRepository catalogs;
    private final RoleRepository roles;
    private final AuthorityRepository authorities;
    private final RoleDeclarationRepository declarations;
    private final IdGenerator ids;

    public RegisterRoleDeclarationService(
            SecurityCatalogRepository catalogs,
            RoleRepository roles,
            AuthorityRepository authorities,
            RoleDeclarationRepository declarations,
            IdGenerator ids
    ) {
        this.catalogs = catalogs;
        this.roles = roles;
        this.authorities = authorities;
        this.declarations = declarations;
        this.ids = ids;
    }

    @Override
    public RegisterRoleDeclarationResult execute(RegisterRoleDeclarationCommand command) {
        RoleDeclaration declaration = command.declaration();
        String calculatedDigest = declaration.contentDigest();
        String suppliedDigest = command.suppliedContentDigest();
        Instant receivedAt = command.publishedAt() == null ? Instant.now() : command.publishedAt();
        Optional<RoleDeclarationReceipt> receipt = declarations.findReceipt(command.eventId());
        if (receipt.isPresent()) {
            RoleDeclarationReceipt existingReceipt = receipt.get();
            if (!sameDelivery(existingReceipt, declaration, suppliedDigest)) {
                recordConflict(command, calculatedDigest, existingReceipt.contentDigest(),
                        "Event identifier was reused for different role declaration content");
                return result(declaration, RoleDeclarationCandidateStatus.QUARANTINED,
                        "Event identifier conflicts with a previously received declaration");
            }
            if (!command.revalidation()) {
                return result(declaration, existingReceipt.status(), null);
            }
        }

        if (!isValidDigest(declaration, suppliedDigest)) {
            recordConflict(command, calculatedDigest, null, "Supplied declaration digest does not match payload");
            saveReceipt(command, RoleDeclarationCandidateStatus.QUARANTINED, receivedAt);
            return result(declaration, RoleDeclarationCandidateStatus.QUARANTINED, "Declaration digest mismatch");
        }

        SecurityCatalog catalog = catalogs.loadForUpdate();
        Optional<RoleDeclarationCandidate> existingCandidate = declarations.findCandidate(
                declaration.owner(), declaration.roleCode(), declaration.declarationRevision());
        if (existingCandidate.isPresent()) {
            RoleDeclarationCandidate candidate = existingCandidate.get();
            if (!candidate.contentDigest().equals(calculatedDigest)) {
                recordConflict(command, calculatedDigest, candidate.contentDigest(),
                        "Declaration revision was received with different content");
                saveReceipt(command, RoleDeclarationCandidateStatus.QUARANTINED, receivedAt);
                return result(declaration, RoleDeclarationCandidateStatus.QUARANTINED,
                        "Declaration revision conflicts with stored content");
            }
            if (candidate.status() != RoleDeclarationCandidateStatus.WAITING_DEPENDENCY
                    || !command.revalidation()) {
                if (receipt.isPresent()) {
                    saveReceipt(command, candidate.status(), receivedAt);
                }
                return result(declaration, candidate.status(), candidate.reason());
            }
        }

        int highestAcceptedRevision = declarations.highestAcceptedRevision(declaration.owner(), declaration.roleCode());
        Optional<RoleDeclarationState> existingState = declarations.findState(declaration.roleCode());
        if (declaration.declarationRevision() < highestAcceptedRevision
                || existingState.isPresent()
                && declaration.declarationRevision() < existingState.get().appliedRevision()) {
            RoleDeclarationCandidate superseded = existingCandidate.orElseGet(() -> new RoleDeclarationCandidate(
                    command.eventId(), declaration, calculatedDigest, RoleDeclarationCandidateStatus.RECEIVED,
                    null, receivedAt, null));
            superseded = superseded.decide(RoleDeclarationCandidateStatus.SUPERSEDED,
                    "A higher declaration revision has already been accepted", Instant.now());
            declarations.saveCandidate(superseded);
            saveReceipt(command, RoleDeclarationCandidateStatus.SUPERSEDED, receivedAt);
            return result(declaration, RoleDeclarationCandidateStatus.SUPERSEDED, superseded.reason());
        }

        Role role = roles.findByCode(declaration.roleCode()).orElse(null);
        if (role != null && role.getKind() != RoleKind.SYSTEM) {
            return quarantineRole(command, calculatedDigest, receivedAt, existingCandidate,
                    "Role code is already owned by a custom role");
        }
        if (existingState.isPresent() && !existingState.get().owner().equals(declaration.owner())) {
            return quarantineRole(command, calculatedDigest, receivedAt, existingCandidate,
                    "Role code is already bound to a different owner");
        }
        boolean roleCreated = role == null;
        if (role == null) {
            role = Role.createSystemPending(ids.generateId(), declaration.roleCode(), declaration.roleCode(), null);
        }

        RoleDeclarationState state = existingState.orElseGet(() -> new RoleDeclarationState(
                declaration.roleCode(), declaration.owner(), declaration.assignmentScopeKey(), 0, ""));
        if (existingState.isEmpty()) {
            role.suspendUntilDeclared(false);
            declarations.saveState(state);
        }

        Set<String> requestedCodes = new HashSet<>();
        declaration.permissions().forEach(reference -> requestedCodes.add(reference.code()));
        Set<Authority> activeAuthorities = authorities.findActiveByCodes(requestedCodes);
        ReconciliationOutcome outcome =
                ProtectedRoleReconciliationPolicy.evaluate(declaration, catalog, activeAuthorities);
        RoleDeclarationCandidate candidate = existingCandidate.orElseGet(() -> new RoleDeclarationCandidate(
                command.eventId(), declaration, calculatedDigest, RoleDeclarationCandidateStatus.RECEIVED,
                null, receivedAt, null));

        if (outcome.status() == ProtectedRoleReconciliationPolicy.ReconciliationStatus.INVALID_DECLARATION) {
            String reason = outcome.reason();
            if (roleCreated || existingState.isEmpty()) {
                roles.save(role);
            }
            declarations.recordConflict(command.eventId(), declaration.owner(), declaration.roleCode(),
                    declaration.declarationRevision(), suppliedDigest, null, payload(declaration), reason);
            RoleDeclarationCandidate quarantined = candidate.decide(
                    RoleDeclarationCandidateStatus.QUARANTINED, reason, Instant.now());
            declarations.saveCandidate(quarantined);
            saveReceipt(command, RoleDeclarationCandidateStatus.QUARANTINED, receivedAt);
            return result(declaration, quarantined.status(), reason);
        }

        if (outcome.isReconciled()) {
            if (declaration.declarationRevision() > state.appliedRevision()) {
                role.reconcileSystemAuthorities(outcome.authorities());
                roles.save(role);
                RoleDeclarationState appliedState = state.applied(
                        declaration.declarationRevision(), calculatedDigest, declaration.assignmentScopeKey());
                declarations.saveState(appliedState);
            }
            RoleDeclarationCandidate applied = candidate.decide(
                    RoleDeclarationCandidateStatus.APPLIED, null, Instant.now());
            declarations.saveCandidate(applied);
            saveReceipt(command, RoleDeclarationCandidateStatus.APPLIED, receivedAt);
            log.info("Role declaration applied owner={} roleCode={} revision={} permissions={}",
                    declaration.owner(), declaration.roleCode(), declaration.declarationRevision(),
                    outcome.authorities().size());
            return result(declaration, applied.status(), null);
        }

        role.suspendUntilDeclared(state.appliedRevision() > 0);
        roles.save(role);
        RoleDeclarationCandidate waiting = candidate.decide(
                RoleDeclarationCandidateStatus.WAITING_DEPENDENCY, outcome.reason(), Instant.now());
        declarations.saveCandidate(waiting);
        saveReceipt(command, RoleDeclarationCandidateStatus.WAITING_DEPENDENCY, receivedAt);
        log.info("Role declaration waiting owner={} roleCode={} revision={} reason={}",
                declaration.owner(), declaration.roleCode(), declaration.declarationRevision(), outcome.reason());
        return result(declaration, waiting.status(), waiting.reason());
    }

    private RegisterRoleDeclarationResult quarantineRole(
            RegisterRoleDeclarationCommand command,
            String digest,
            Instant receivedAt,
            Optional<RoleDeclarationCandidate> existingCandidate,
            String reason
    ) {
        RoleDeclaration declaration = command.declaration();
        declarations.recordConflict(command.eventId(), declaration.owner(), declaration.roleCode(),
                declaration.declarationRevision(), digest, null, payload(declaration), reason);
        RoleDeclarationCandidate candidate = existingCandidate.orElseGet(() -> new RoleDeclarationCandidate(
                command.eventId(), declaration, digest, RoleDeclarationCandidateStatus.RECEIVED,
                null, receivedAt, null));
        RoleDeclarationCandidate quarantined = candidate.decide(
                RoleDeclarationCandidateStatus.QUARANTINED, reason, Instant.now());
        declarations.saveCandidate(quarantined);
        saveReceipt(command, RoleDeclarationCandidateStatus.QUARANTINED, receivedAt);
        return result(declaration, RoleDeclarationCandidateStatus.QUARANTINED, reason);
    }

    private void recordConflict(
            RegisterRoleDeclarationCommand command,
            String calculatedDigest,
            String existingDigest,
            String reason
    ) {
        RoleDeclaration declaration = command.declaration();
        declarations.recordConflict(command.eventId(), declaration.owner(), declaration.roleCode(),
                declaration.declarationRevision(), command.suppliedContentDigest(), existingDigest,
                payload(declaration), reason);
        if (!calculatedDigest.equals(command.suppliedContentDigest())) {
            log.warn("Quarantined role declaration owner={} roleCode={} revision={} reason={}",
                    declaration.owner(), declaration.roleCode(), declaration.declarationRevision(), reason);
        }
    }

    private void saveReceipt(
            RegisterRoleDeclarationCommand command,
            RoleDeclarationCandidateStatus status,
            Instant receivedAt
    ) {
        RoleDeclaration declaration = command.declaration();
        RoleDeclarationReceipt receipt = new RoleDeclarationReceipt(
                command.eventId(), declaration.owner(), declaration.roleCode(),
                declaration.declarationRevision(), declaration.contentDigest(), status, receivedAt);
        declarations.saveReceipt(receipt);
    }

    private boolean sameDelivery(
            RoleDeclarationReceipt receipt,
            RoleDeclaration declaration,
            String suppliedDigest
    ) {
        return receipt.owner().equals(declaration.owner())
                && receipt.roleCode().equals(declaration.roleCode())
                && receipt.revision() == declaration.declarationRevision()
                && receipt.contentDigest().equals(declaration.contentDigest())
                && isValidDigest(declaration, suppliedDigest);
    }

    private boolean isValidDigest(RoleDeclaration declaration, String suppliedDigest) {
        return declaration.contentDigest().equals(suppliedDigest)
                || declaration.legacyContentDigest().equals(suppliedDigest);
    }

    private String payload(RoleDeclaration declaration) {
        return declaration.toString();
    }

    private RegisterRoleDeclarationResult result(
            RoleDeclaration declaration,
            RoleDeclarationCandidateStatus status,
            String reason
    ) {
        return new RegisterRoleDeclarationResult(
                declaration.roleCode(), declaration.owner(), declaration.declarationRevision(),
                status.name(), reason);
    }
}
