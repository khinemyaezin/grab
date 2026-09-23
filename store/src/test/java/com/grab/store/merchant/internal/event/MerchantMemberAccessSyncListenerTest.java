package com.grab.store.merchant.internal.event;

import com.grab.store.identity.port.AccessManagementPort;
import com.merchant.domain.event.MerchantAccessProfile;
import com.merchant.domain.event.MerchantMemberCreatedEvent;
import com.merchant.domain.event.MerchantMemberRemovedEvent;
import com.merchant.domain.event.MerchantMemberRoleChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MerchantMemberAccessSyncListenerTest {
    private AccessManagementPort accessManagementPort;
    private MerchantMemberAccessSyncListener listener;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        accessManagementPort = mock(AccessManagementPort.class);
        listener = new MerchantMemberAccessSyncListener(accessManagementPort);
    }

    @Test
    void onMemberCreated_whenActiveAdmin_shouldAssignAdminRole() {
        MerchantMemberCreatedEvent event = new MerchantMemberCreatedEvent(
                "mem-1", "mer-1", "usr-1", "MERCHANT_ADMIN", Set.of("*"), true, "ACTIVE", null, 1, now
        );

        listener.onMemberCreated(event);

        ArgumentCaptor<AccessManagementPort.ReplaceAccessRequest> captor =
                ArgumentCaptor.forClass(AccessManagementPort.ReplaceAccessRequest.class);
        verify(accessManagementPort).replaceAccess(captor.capture());

        AccessManagementPort.ReplaceAccessRequest request = captor.getValue();
        assertThat(request.userId()).isEqualTo("usr-1");
        assertThat(request.platformCode()).isEqualTo(MerchantAccessProfile.SELLER_PLATFORM_CODE);
        assertThat(request.previousRoleCode()).isNull();
        assertThat(request.roleCode()).isEqualTo(MerchantAccessProfile.ADMIN_ROLE_CODE);
        assertThat(request.scopeKey()).isEqualTo(MerchantAccessProfile.MERCHANT_SCOPE_KEY);
        assertThat(request.scopeId()).isEqualTo("mer-1");
    }

    @Test
    void onMemberCreated_whenInvitedMember_shouldNotReplaceAccess() {
        MerchantMemberCreatedEvent event = new MerchantMemberCreatedEvent(
                "mem-2", "mer-1", "usr-2", "OPERATOR", Set.of(), false, "INVITED", "usr-1", 1, now
        );

        listener.onMemberCreated(event);

        verify(accessManagementPort, never()).replaceAccess(any());
    }

    @Test
    void onMemberRoleChanged_shouldTriggerReplaceAccess() {
        MerchantMemberRoleChangedEvent event = new MerchantMemberRoleChangedEvent(
                "mem-1", "mer-1", "usr-1", "OPERATOR", "ANALYST", 2, now
        );

        listener.onMemberRoleChanged(event);

        ArgumentCaptor<AccessManagementPort.ReplaceAccessRequest> captor =
                ArgumentCaptor.forClass(AccessManagementPort.ReplaceAccessRequest.class);
        verify(accessManagementPort).replaceAccess(captor.capture());

        AccessManagementPort.ReplaceAccessRequest request = captor.getValue();
        assertThat(request.userId()).isEqualTo("usr-1");
        assertThat(request.previousRoleCode()).isEqualTo("OPERATOR");
        assertThat(request.roleCode()).isEqualTo("ANALYST");
    }

    @Test
    void onMemberRemoved_shouldTriggerRevokeAccess() {
        MerchantMemberRemovedEvent event = new MerchantMemberRemovedEvent(
                "mem-1", "mer-1", "usr-1", "OPERATOR", 3, now
        );

        listener.onMemberRemoved(event);

        ArgumentCaptor<AccessManagementPort.RevokeAccessRequest> captor =
                ArgumentCaptor.forClass(AccessManagementPort.RevokeAccessRequest.class);
        verify(accessManagementPort).revokeAccess(captor.capture());

        AccessManagementPort.RevokeAccessRequest request = captor.getValue();
        assertThat(request.userId()).isEqualTo("usr-1");
        assertThat(request.roleCode()).isEqualTo("OPERATOR");
        assertThat(request.scopeKey()).isEqualTo(MerchantAccessProfile.MERCHANT_SCOPE_KEY);
        assertThat(request.scopeId()).isEqualTo("mer-1");
    }
}
