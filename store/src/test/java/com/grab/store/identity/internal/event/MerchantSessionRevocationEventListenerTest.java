package com.grab.store.identity.internal.event;

import com.grab.store.merchant.events.MerchantClosedIntegrationEvent;
import com.grab.store.merchant.events.MerchantSuspendedIntegrationEvent;
import com.grab.store.merchant.events.StorefrontStatusChangedIntegrationEvent;
import com.grab.store.shared.security.PlatformScopes;
import com.identity.domain.port.outbound.SessionStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantSessionRevocationEventListenerTest {

    @Test
    void merchantSuspendAndCloseRevokeSellerPortalAccountSessions() {
        RecordingSessionStore sessions = new RecordingSessionStore();
        MerchantSessionRevocationEventListener listener = new MerchantSessionRevocationEventListener(sessions);

        listener.onMerchantSuspended(new MerchantSuspendedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "SUSPENDED", Instant.now(), 1));
        listener.onMerchantClosed(new MerchantClosedIntegrationEvent(
                "m-1", "u-1", "Shop", "FIRST_PARTY_RETAILER", "CLOSED", Instant.now(), 2));

        assertThat(sessions.revocations).containsExactly(
                new ScopeRevocation(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "m-1"),
                new ScopeRevocation(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_ACCOUNT_SCOPE, "m-1")
        );
    }

    @Test
    void storefrontSuspendRevokesStorefrontScopeAndActivateIsNoOp() {
        RecordingSessionStore sessions = new RecordingSessionStore();
        MerchantSessionRevocationEventListener listener = new MerchantSessionRevocationEventListener(sessions);

        listener.onStorefrontStatusChanged(new StorefrontStatusChangedIntegrationEvent(
                "sf-1", "m-1", "shop", "DRAFT", "ACTIVE", Instant.now(), 1));
        listener.onStorefrontStatusChanged(new StorefrontStatusChangedIntegrationEvent(
                "sf-1", "m-1", "shop", "ACTIVE", "SUSPENDED", Instant.now(), 2));
        listener.onStorefrontStatusChanged(new StorefrontStatusChangedIntegrationEvent(
                "sf-1", "m-1", "shop", "SUSPENDED", "CLOSED", Instant.now(), 3));

        assertThat(sessions.revocations).containsExactly(
                new ScopeRevocation(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_STOREFRONT_SCOPE, "sf-1"),
                new ScopeRevocation(PlatformScopes.SELLER_PORTAL, PlatformScopes.MERCHANT_STOREFRONT_SCOPE, "sf-1")
        );
    }

    private record ScopeRevocation(String platformCode, String scopeKey, String scopeId) {
    }

    private static final class RecordingSessionStore implements SessionStore {
        private final List<ScopeRevocation> revocations = new ArrayList<>();

        @Override
        public void saveNewSession(
                String userId,
                String tokenHash,
                String tokenFamilyId,
                Instant expiresAt,
                com.grab.framework.security.AccessContext accessContext
        ) {
        }

        @Override
        public java.util.Optional<com.identity.domain.valueobject.SessionDetails> findByTokenHash(String tokenHash) {
            return java.util.Optional.empty();
        }

        @Override
        public void revokeFamily(String tokenFamilyId) {
        }

        @Override
        public void revokeSession(String tokenHash) {
        }

        @Override
        public void revokeAll(String userId) {
        }

        @Override
        public void revokeByAssignment(String assignmentId) {
        }

        @Override
        public void revokeByScope(String platformCode, String scopeKey, String scopeId) {
            revocations.add(new ScopeRevocation(platformCode, scopeKey, scopeId));
        }

        @Override
        public void replaceSession(String oldTokenHash, String newTokenHash, Instant oldRevokedAt) {
        }
    }
}
