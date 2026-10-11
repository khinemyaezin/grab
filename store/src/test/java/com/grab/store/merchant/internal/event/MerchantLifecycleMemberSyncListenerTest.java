package com.grab.store.merchant.internal.event;

import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MerchantLifecycleMemberSyncListenerTest {
    private IdentityAccessManagementPort identityAccessManagementPort;
    private MerchantLifecycleMemberSyncListener listener;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        identityAccessManagementPort = mock(IdentityAccessManagementPort.class);
        listener = new MerchantLifecycleMemberSyncListener(identityAccessManagementPort);
    }

    @Test
    void onMerchantSuspended_whenInvoked_revokesSessionsForMerchantScope() {
        MerchantSuspendedEvent event = new MerchantSuspendedEvent(
                "mer-1", "Test Merchant", "usr-1", "SUSPENDED", "operator-1", 2, now
        );

        listener.onMerchantSuspended(event);

        verify(identityAccessManagementPort).revokeSessionsByScope(
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                "mer-1"
        );
    }

    @Test
    void onMerchantClosed_whenInvoked_revokesSessionsForMerchantScope() {
        MerchantClosedEvent event = new MerchantClosedEvent(
                "mer-1", "Test Merchant", "usr-1", "CLOSED", "operator-1", 3, now
        );

        listener.onMerchantClosed(event);

        verify(identityAccessManagementPort).revokeSessionsByScope(
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                "mer-1"
        );
    }
}
