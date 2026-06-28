package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.enums.AccessAssignmentStatus;
import com.identity.domain.event.AccessAssignmentChangedEvent;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.ScopeKey;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessAssignmentTest {
    @Test
    void create_withValidScope_shouldCreateActiveAssignmentAndEmitEvent() {
        AccessAssignment assignment = AccessAssignment.create(
                new CommonId("assignment-1"),
                new CommonId("user-1"),
                sellerPlatform(),
                " merchant_owner ",
                new AccessScope(new ScopeKey("merchant.account"), "merchant-1"),
                new CommonId("admin-1"),
                Instant.now().plusSeconds(3600)
        );

        assertEquals(AccessAssignmentStatus.ACTIVE, assignment.getStatus());
        assertEquals("SELLER_PORTAL", assignment.getPlatformCode());
        assertEquals("MERCHANT_OWNER", assignment.getRoleCode());
        assertTrue(assignment.isEffectiveAt(Instant.now()));
        assertInstanceOf(AccessAssignmentChangedEvent.class, assignment.getEvents().getFirst());
    }

    @Test
    void suspend_withActiveAssignment_shouldSuspendAndStopEffectiveAccess() {
        AccessAssignment assignment = activeAssignment();

        assignment.suspend();

        assertEquals(AccessAssignmentStatus.SUSPENDED, assignment.getStatus());
        assertFalse(assignment.isEffectiveAt(Instant.now()));
    }

    @Test
    void reactivate_withSuspendedAssignment_shouldActivate() {
        AccessAssignment assignment = activeAssignment();
        assignment.suspend();

        assignment.changeStatus(AccessAssignmentStatus.ACTIVE, new CommonId("admin-1"));

        assertEquals(AccessAssignmentStatus.ACTIVE, assignment.getStatus());
    }

    @Test
    void create_withPastExpiration_shouldRejectAssignment() {
        assertThrows(
                IdentityDomainValidationException.class,
                () -> AccessAssignment.create(
                        new CommonId("assignment-1"),
                        new CommonId("user-1"),
                        sellerPlatform(),
                        "MERCHANT_OWNER",
                        new AccessScope(new ScopeKey("merchant.account"), "merchant-1"),
                        null,
                        Instant.now().minusSeconds(1)
                )
        );
    }

    @Test
    void accessScope_withGlobalTypeAndResourceId_shouldRejectScope() {
        assertThrows(
                IdentityDomainValidationException.class,
                () -> new AccessScope(ScopeKey.global(), "merchant-1")
        );
    }

    @Test
    void expireIfDue_withExpiredActiveAssignment_shouldExpireAndEmitEvent() {
        Instant now = Instant.now();
        AccessAssignment assignment = new AccessAssignment(
                new CommonId("assignment-1"),
                new CommonId("user-1"),
                "SELLER_PORTAL",
                "MERCHANT_OWNER",
                new AccessScope(new ScopeKey("merchant.account"), "merchant-1"),
                AccessAssignmentStatus.ACTIVE,
                new CommonId("admin-1"),
                now.minusSeconds(120),
                now.minusSeconds(120),
                now.minusSeconds(60)
        );

        assertTrue(assignment.expireIfDue(now));

        assertEquals(AccessAssignmentStatus.EXPIRED, assignment.getStatus());
        assertInstanceOf(AccessAssignmentChangedEvent.class, assignment.getEvents().getFirst());
    }

    @Test
    void reactivate_withExpiredSuspendedAssignment_shouldRemainExpired() {
        Instant now = Instant.now();
        AccessAssignment assignment = new AccessAssignment(
                new CommonId("assignment-1"),
                new CommonId("user-1"),
                "SELLER_PORTAL",
                "MERCHANT_OWNER",
                new AccessScope(new ScopeKey("merchant.account"), "merchant-1"),
                AccessAssignmentStatus.SUSPENDED,
                new CommonId("admin-1"),
                now.minusSeconds(120),
                now.minusSeconds(120),
                now.minusSeconds(60)
        );

        assignment.changeStatus(AccessAssignmentStatus.ACTIVE, new CommonId("admin-1"));

        assertEquals(AccessAssignmentStatus.EXPIRED, assignment.getStatus());
    }

    @Test
    void create_withUnsupportedPlatformRole_shouldRejectAssignment() {
        assertThrows(
                IdentityDomainValidationException.class,
                () -> AccessAssignment.create(
                        new CommonId("assignment-1"),
                        new CommonId("user-1"),
                        sellerPlatform(),
                        "SUPER_ADMIN",
                        new AccessScope(new ScopeKey("merchant.account"), "merchant-1"),
                        new CommonId("admin-1"),
                        null
                )
        );
    }

    @Test
    void create_whenActorAssignsThemself_shouldRejectAssignment() {
        assertThrows(
                IdentityDomainValidationException.class,
                () -> AccessAssignment.create(
                        new CommonId("assignment-1"),
                        new CommonId("user-1"),
                        sellerPlatform(),
                        "MERCHANT_OWNER",
                        new AccessScope(new ScopeKey("merchant.account"), "merchant-1"),
                        new CommonId("user-1"),
                        null
                )
        );
    }

    private AccessAssignment activeAssignment() {
        Instant now = Instant.now();
        return new AccessAssignment(
                new CommonId("assignment-1"),
                new CommonId("user-1"),
                "SELLER_PORTAL",
                "MERCHANT_OWNER",
                new AccessScope(new ScopeKey("merchant.account"), "merchant-1"),
                AccessAssignmentStatus.ACTIVE,
                new CommonId("admin-1"),
                now,
                now,
                null
        );
    }

    private Platform sellerPlatform() {
        return new Platform(
                new CommonId("seller-platform"),
                "SELLER_PORTAL",
                "Seller Portal",
                true,
                java.util.Set.of("MERCHANT_OWNER")
        );
    }
}
