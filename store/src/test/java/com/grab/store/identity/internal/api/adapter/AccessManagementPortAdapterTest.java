package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.port.AccessManagementPort;
import com.identity.application.model.write.ReplaceAccessCommand;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import com.identity.application.port.inbound.ReplaceAccessUseCase;
import com.identity.application.port.inbound.RevokeSessionsByScopeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessManagementPortAdapterTest {

    @Mock
    private ReplaceAccessUseCase replaceAccessUseCase;

    @Mock
    private RevokeSessionsByScopeUseCase revokeSessionsByScopeUseCase;

    @Mock
    private IdGenerator idGenerator;

    private AccessManagementPortAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccessManagementPortAdapter(
                replaceAccessUseCase,
                revokeSessionsByScopeUseCase,
                idGenerator
        );
    }

    @Test
    void shouldPassPreviousRoleAndReplacementRole_onReplaceAccess() {
        Id userId = () -> "usr-123";
        when(idGenerator.convertIdFrom("usr-123")).thenReturn(userId);

        var request = new AccessManagementPort.ReplaceAccessRequest(
                "usr-123",
                "SELLER_PORTAL",
                "MERCHANT_STAFF",
                "MERCHANT_OWNER",
                "merchant.account",
                "store-999"
        );

        adapter.replaceAccess(request);

        ArgumentCaptor<ReplaceAccessCommand> captor = ArgumentCaptor.forClass(ReplaceAccessCommand.class);
        verify(replaceAccessUseCase).execute(captor.capture());

        ReplaceAccessCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.platformCode()).isEqualTo("SELLER_PORTAL");
        assertThat(command.previousRoleCode()).isEqualTo("MERCHANT_STAFF");
        assertThat(command.replacementRoleCode()).isEqualTo("MERCHANT_OWNER");
        assertThat(command.scopeKey()).isEqualTo("merchant.account");
        assertThat(command.scopeId()).isEqualTo("store-999");
    }

    @Test
    void shouldPassRoleCodeAsPreviousRole_onRevokeAccess() {
        Id userId = () -> "usr-123";
        when(idGenerator.convertIdFrom("usr-123")).thenReturn(userId);

        var request = new AccessManagementPort.RevokeAccessRequest(
                "usr-123",
                "SELLER_PORTAL",
                "MERCHANT_STAFF",
                "merchant.account",
                "store-999"
        );

        adapter.revokeAccess(request);

        ArgumentCaptor<ReplaceAccessCommand> captor = ArgumentCaptor.forClass(ReplaceAccessCommand.class);
        verify(replaceAccessUseCase).execute(captor.capture());

        ReplaceAccessCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.platformCode()).isEqualTo("SELLER_PORTAL");
        assertThat(command.previousRoleCode()).isEqualTo("MERCHANT_STAFF");
        assertThat(command.replacementRoleCode()).isNull();
        assertThat(command.scopeKey()).isEqualTo("merchant.account");
        assertThat(command.scopeId()).isEqualTo("store-999");
    }

    @Test
    void shouldRevokeSessionsByScope() {
        adapter.revokeSessionsByScope("SELLER_PORTAL", "merchant.account", "store-999");

        ArgumentCaptor<RevokeSessionsByScopeCommand> captor = ArgumentCaptor.forClass(RevokeSessionsByScopeCommand.class);
        verify(revokeSessionsByScopeUseCase).execute(captor.capture());

        RevokeSessionsByScopeCommand command = captor.getValue();
        assertThat(command.platformCode()).isEqualTo("SELLER_PORTAL");
        assertThat(command.scopeKey()).isEqualTo("merchant.account");
        assertThat(command.scopeId()).isEqualTo("store-999");
    }
}
