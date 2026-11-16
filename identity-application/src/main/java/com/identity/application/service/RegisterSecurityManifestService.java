package com.identity.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifest;
import com.identity.application.model.write.RegisterSecurityManifestCommand;
import com.identity.application.model.write.RegisterSecurityManifestResult;
import com.identity.application.port.inbound.RegisterSecurityManifestUseCase;
import com.identity.application.port.outbound.EventDeliveryIdPort;
import com.identity.domain.aggregate.SecurityCatalog;
import com.identity.domain.port.outbound.SecurityCatalogRepository;
import com.identity.domain.port.outbound.SecurityManifestInboxRepository;
import com.identity.domain.port.outbound.SecurityManifestRevisionRepository;
import com.identity.domain.security.*;
import lombok.AllArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
public class RegisterSecurityManifestService implements RegisterSecurityManifestUseCase {
    private static final Logger log = Loggers.getLogger(RegisterSecurityManifestService.class);
    private final SecurityCatalogRepository catalogs;
    private final SecurityManifestRevisionRepository revisions;
    private final SecurityManifestInboxRepository inbox;
    private final EventDeliveryIdPort deliveryIds;

    @Override
    public RegisterSecurityManifestResult execute(RegisterSecurityManifestCommand command) {
        String eventId = deliveryIds.requireEventId().toString();
        return register(command.manifest(), command.suppliedContentDigest(), eventId);
    }

    RegisterSecurityManifestResult revalidate(SecurityManifestCandidate candidate) {
        return register(candidate.manifest(), candidate.manifest().contentDigest(), candidate.eventId());
    }

    private RegisterSecurityManifestResult register(SecurityManifest manifest, String suppliedDigest, String eventId) {
        SecurityCatalog catalog = catalogs.loadForUpdate();
        var receipt = inbox.find(eventId);
        var canonical = revisions.find(manifest.moduleKey(), manifest.securityRevision());
        int highestAccepted = revisions.highestAcceptedRevision(manifest.moduleKey());
        var decision = catalog.consider(manifest, suppliedDigest, eventId,
                receipt, canonical, highestAccepted);
        String errorCode = decision.errorCode();
        if (errorCode == null && !decision.newlyActivated() && canonical.isPresent()
                && canonical.get().status() == decision.status()) {
            errorCode = canonical.get().errorCode();
        }
        if (decision.conflict()) {
            inbox.recordConflict(eventId, manifest, suppliedDigest, errorCode);
        } else {
            Instant now = Instant.now();
            var candidate = canonical.orElseGet(() -> new SecurityManifestCandidate(eventId, manifest,
                    SecurityManifestCandidateStatus.RECEIVED, null, now, null));
            var decided = candidate.decide(decision, now);
            revisions.save(decided);
            var outcome = new SecurityManifestReceipt(eventId, manifest.moduleKey(), manifest.securityRevision(),
                    manifest.contentDigest(), decision.status(), errorCode);
            inbox.save(outcome);
            if (decision.newlyActivated()) {
                catalogs.save(catalog);
            }
        }
        log.info("Security manifest module: {}, revision: {}, outcome: {}, errorCode: {}",
                manifest.moduleKey(), manifest.securityRevision(), decision.status(), errorCode);
        String digest = manifest.contentDigest();
        String status = decision.status().name();
        return new RegisterSecurityManifestResult(manifest.moduleKey(), manifest.securityRevision(), digest,
                status, errorCode, decision.newlyActivated());
    }
}
