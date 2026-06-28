package com.identity.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.identity.domain.enums.AccessScopeType;
import com.identity.domain.enums.InvitationStatus;
import com.identity.domain.event.AccessInvitationChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessInvitationTest {
    private static final String TOKEN_HASH = "a".repeat(64);

    @Test
    void create_withValidDetails_shouldCreatePendingInvitation() {
        AccessInvitation invitation = AccessInvitation.create(
                new CommonId("invitation-1"),
                new Email("STAFF@example.com"),
                sellerPlatform(),
                "STOREFRONT_MANAGER",
                new AccessScope(AccessScopeType.STOREFRONT, "storefront-1"),
                TOKEN_HASH,
                new CommonId("owner-1"),
                new Email("owner@example.com"),
                Instant.now().plusSeconds(3600)
        );

        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        assertEquals("staff@example.com", invitation.getInviteeEmail().value());
        assertInstanceOf(AccessInvitationChangedEvent.class, invitation.getEvents().getFirst());
    }

    @Test
    void accept_withPendingInvitation_shouldRecordAcceptedUser() {
        AccessInvitation invitation = pendingInvitation(Instant.now().plusSeconds(3600));

        invitation.accept(new CommonId("staff-1"), new Email("staff@example.com"), Instant.now());

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals("staff-1", invitation.getAcceptedBy().getValue());
    }

    @Test
    void accept_withExpiredInvitation_shouldRejectAcceptance() {
        AccessInvitation invitation = pendingInvitation(Instant.now().minusSeconds(1));

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> invitation.accept(
                        new CommonId("staff-1"), new Email("staff@example.com"), Instant.now()
                )
        );

        assertInstanceOf(IdentityDomainError.AccessInvitationExpired.class, exception.getMessageSource());
    }

    @Test
    void create_withSameInviterAndInviteeEmail_shouldRejectSelfInvitation() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> AccessInvitation.create(
                        new CommonId("invitation-1"),
                        new Email("owner@example.com"),
                        sellerPlatform(),
                        "STOREFRONT_MANAGER",
                        new AccessScope(AccessScopeType.STOREFRONT, "storefront-1"),
                        TOKEN_HASH,
                        new CommonId("owner-1"),
                        new Email("owner@example.com"),
                        Instant.now().plusSeconds(3600)
                )
        );

        assertInstanceOf(IdentityDomainError.SelfAccessInvitationForbidden.class,
                exception.getMessageSource());
    }

    @Test
    void accept_withDifferentRecipientEmail_shouldRejectAcceptance() {
        AccessInvitation invitation = pendingInvitation(Instant.now().plusSeconds(3600));

        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> invitation.accept(
                        new CommonId("other-user"), new Email("other@example.com"), Instant.now()
                )
        );

        assertInstanceOf(IdentityDomainError.AccessInvitationRecipientMismatch.class,
                exception.getMessageSource());
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
    }

    @Test
    void create_withUnsupportedPlatformRole_shouldRejectInvitation() {
        IdentityDomainValidationException exception = assertThrows(
                IdentityDomainValidationException.class,
                () -> AccessInvitation.create(
                        new CommonId("invitation-1"),
                        new Email("staff@example.com"),
                        sellerPlatform(),
                        "SUPER_ADMIN",
                        new AccessScope(AccessScopeType.STOREFRONT, "storefront-1"),
                        TOKEN_HASH,
                        new CommonId("owner-1"),
                        new Email("owner@example.com"),
                        Instant.now().plusSeconds(3600)
                )
        );

        assertInstanceOf(IdentityDomainError.PlatformRoleNotSupported.class,
                exception.getMessageSource());
    }

    private AccessInvitation pendingInvitation(Instant expiresAt) {
        Instant createdAt = expiresAt.minusSeconds(3600);
        return new AccessInvitation(
                new CommonId("invitation-1"),
                new Email("staff@example.com"),
                "SELLER_PORTAL",
                "STOREFRONT_MANAGER",
                new AccessScope(AccessScopeType.STOREFRONT, "storefront-1"),
                TOKEN_HASH,
                new CommonId("owner-1"),
                InvitationStatus.PENDING,
                createdAt,
                expiresAt,
                null,
                createdAt
        );
    }

    private Platform sellerPlatform() {
        return new Platform(
                new CommonId("seller-platform"),
                "SELLER_PORTAL",
                "Seller Portal",
                true,
                java.util.Set.of("STOREFRONT_MANAGER")
        );
    }
}
