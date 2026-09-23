package com.grab.store.merchant.internal.event;

import com.grab.store.identity.port.AccessManagementPort;
import com.merchant.domain.event.MerchantAccessProfile;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MerchantLifecycleMemberSyncListenerTest {
    private AccessManagementPort accessManagementPort;
    private MerchantLifecycleMemberSyncListener listener;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        accessManagementPort = mock(AccessManagementPort.class);
        listener = new MerchantLifecycleMemberSyncListener(accessManagementPort);
    }

    @Test
    void onMerchantSuspended_shouldRevokeSessionsForMerchantScope() {
        MerchantSuspendedEvent event = new MerchantSuspendedEvent(
                "mer-1", "Test Merchant", "usr-1", "SUSPENDED", "operator-1", 2, now
        );

        listener.onMerchantSuspended(event);

        verify(accessManagementPort).revokeSessionsByScope(
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                "mer-1"
        );
    }

    @Test
    void onMerchantClosed_shouldRevokeSessionsForMerchantScope() {
        MerchantClosedEvent event = new MerchantClosedEvent(
                "mer-1", "Test Merchant", "usr-1", "CLOSED", "operator-1", 3, now
        );

        listener.onMerchantClosed(event);

        verify(accessManagementPort).revokeSessionsByScope(
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                "mer-1"
        );
    }
}
