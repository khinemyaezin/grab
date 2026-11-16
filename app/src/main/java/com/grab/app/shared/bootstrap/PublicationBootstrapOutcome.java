package com.grab.app.shared.bootstrap;

import com.grab.framework.bootstrap.BootstrapTaskException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;

/**
 * Translates a manifest / role-declaration publication result into a bootstrap outcome.
 * {@code CONFLICT} (same revision, different digest) cannot be fixed by retrying: the task fails permanently and
 * readiness stays DOWN until the revision is bumped. {@code SUPERSEDED} means a newer pod already published a later
 * revision; this (older) pod is logged at WARN but does not block its own rollout.
 */
public final class PublicationBootstrapOutcome {

    private static final Logger log = Loggers.getLogger(PublicationBootstrapOutcome.class);

    private PublicationBootstrapOutcome() {
    }

    public static void require(String subject, SecurityManifestPublicationPort.PublicationResult result) {
        String outcome = result == null ? "null" : result.name();
        require(subject, outcome);
    }

    public static void require(String subject, RoleDeclarationPublicationPort.PublicationResult result) {
        String outcome = result == null ? "null" : result.name();
        require(subject, outcome);
    }

    private static void require(String subject, String outcome) {
        switch (outcome) {
            case "ENQUEUED", "NOT_DUE" -> log.info("Bootstrap publication: {} outcome: {}", subject, outcome);
            case "SUPERSEDED" -> log.warn("Bootstrap publication: {} SUPERSEDED by a newer revision; this replica runs "
                    + "a stale build", subject);
            case "CONFLICT" -> throw BootstrapTaskException.permanent("Publication " + subject
                    + " CONFLICT: same revision published with a different digest; bump the revision");
            default -> throw BootstrapTaskException.transientFailure("Publication " + subject + " outcome: " + outcome);
        }
    }
}
