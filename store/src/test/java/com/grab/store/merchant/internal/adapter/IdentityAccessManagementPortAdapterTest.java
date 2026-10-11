package com.grab.store.merchant.internal.adapter;

import com.grab.store.identity.port.AccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort.ReplaceAccessRequest;
import com.merchant.application.port.outbound.IdentityAccessManagementPort.RevokeAccessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class IdentityAccessManagementPortAdapterTest {

    private AccessManagementPort accessManagementPort;
    private IdentityAccessManagementPortAdapter adapter;

    @BeforeEach
    void setUp() {
        accessManagementPort = Mockito.mock(AccessManagementPort.class);
        adapter = new IdentityAccessManagementPortAdapter(accessManagementPort);
    }

    @Test
    void replaceAccess_whenInvoked_mapsAndDelegatesToIdentityPort() {
        ReplaceAccessRequest request = new ReplaceAccessRequest(
                "usr-1",
                "OPERATOR",
                "ANALYST",
                "merchant.account",
                "mer-1",
                Set.of("PRODUCT_READ")
        );

        adapter.replaceAccess(request);

        ArgumentCaptor<AccessManagementPort.ReplaceAccessRequest> captor =
                ArgumentCaptor.forClass(AccessManagementPort.ReplaceAccessRequest.class);
        verify(accessManagementPort).replaceAccess(captor.capture());
        AccessManagementPort.ReplaceAccessRequest captured = captor.getValue();
        assertThat(captured.userId()).isEqualTo("usr-1");
        assertThat(captured.previousRoleCode()).isEqualTo("OPERATOR");
        assertThat(captured.roleCode()).isEqualTo("ANALYST");
        assertThat(captured.scopeKey()).isEqualTo("merchant.account");
        assertThat(captured.scopeId()).isEqualTo("mer-1");
        assertThat(captured.authorityCodes()).containsExactly("PRODUCT_READ");
    }

    @Test
    void revokeAccess_whenInvoked_mapsAndDelegatesToIdentityPort() {
        RevokeAccessRequest request = new RevokeAccessRequest(
                "usr-1",
                "OPERATOR",
                "merchant.account",
                "mer-1"
        );

        adapter.revokeAccess(request);

        ArgumentCaptor<AccessManagementPort.RevokeAccessRequest> captor =
                ArgumentCaptor.forClass(AccessManagementPort.RevokeAccessRequest.class);
        verify(accessManagementPort).revokeAccess(captor.capture());
        AccessManagementPort.RevokeAccessRequest captured = captor.getValue();
        assertThat(captured.userId()).isEqualTo("usr-1");
        assertThat(captured.roleCode()).isEqualTo("OPERATOR");
        assertThat(captured.scopeKey()).isEqualTo("merchant.account");
        assertThat(captured.scopeId()).isEqualTo("mer-1");
    }

    @Test
    void revokeSessionsByScope_whenInvoked_delegatesToIdentityPort() {
        adapter.revokeSessionsByScope("merchant.account", "mer-1");

        verify(accessManagementPort).revokeSessionsByScope("merchant.account", "mer-1");
    }
}
