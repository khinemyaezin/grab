package com.grab.framework.security.role.policy;

import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RoleDeclarationPublicationPort.PublicationResult;

import java.time.Instant;

public final class RoleDeclarationPublicationPolicy {
    private RoleDeclarationPublicationPolicy() {
    }

    public static PublicationResult decide(
            RoleDeclaration declaration,
            int revision,
            String digest,
            Instant nextPublicationAt,
            Instant now
    ) {
        if (declaration.declarationRevision() < revision) {
            return PublicationResult.SUPERSEDED;
        }
        String candidateDigest = declaration.contentDigest();
        if (declaration.declarationRevision() == revision && !candidateDigest.equals(digest)) {
            return PublicationResult.CONFLICT;
        }
        if (declaration.declarationRevision() == revision && nextPublicationAt != null
                && nextPublicationAt.isAfter(now)) {
            return PublicationResult.NOT_DUE;
        }
        return PublicationResult.ENQUEUED;
    }
}
