package com.grab.framework.security.role.policy;

import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RoleDeclarationPublicationPort.PublicationResult;
import com.grab.framework.security.role.RolePermissionReference;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleDeclarationPublicationPolicyTest {
    private static final Instant NOW = Instant.parse("2026-10-10T00:00:00Z");
    private static final RoleDeclaration DECLARATION = new RoleDeclaration(
            "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
            Map.of("merchant", 1), List.of(new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ")));

    @Test
    void decideEnqueuesFirstPublication() {
        PublicationResult result = RoleDeclarationPublicationPolicy.decide(DECLARATION, 0, "", null, NOW);

        assertEquals(PublicationResult.ENQUEUED, result);
    }

    @Test
    void decidePreventsConflictingRevisionAndThrottlesDuplicatePublication() {
        Instant nextPublicationAt = NOW.plusSeconds(60);
        PublicationResult conflict = RoleDeclarationPublicationPolicy.decide(
                DECLARATION, 1, "different-digest", nextPublicationAt, NOW);
        PublicationResult notDue = RoleDeclarationPublicationPolicy.decide(
                DECLARATION, 1, DECLARATION.contentDigest(), nextPublicationAt, NOW);

        assertEquals(PublicationResult.CONFLICT, conflict);
        assertEquals(PublicationResult.NOT_DUE, notDue);
    }

    @Test
    void decideAllowsNewerRevisionAndRejectsSupersededRevision() {
        RoleDeclaration newerDeclaration = new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 2,
                Map.of("merchant", 1), DECLARATION.permissions());
        PublicationResult newer = RoleDeclarationPublicationPolicy.decide(
                newerDeclaration, 1, DECLARATION.contentDigest(), NOW.plusSeconds(60), NOW);
        PublicationResult older = RoleDeclarationPublicationPolicy.decide(
                DECLARATION, 2, "newer-digest", NOW.plusSeconds(60), NOW);

        assertEquals(PublicationResult.ENQUEUED, newer);
        assertEquals(PublicationResult.SUPERSEDED, older);
    }
}
