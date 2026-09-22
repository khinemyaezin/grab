package com.grab.store.identity.internal.api.adapter;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.identity.port.AccessManagementPort.GrantAccessRequest;
import com.grab.store.identity.port.AccessManagementPort.RevokeAccessRequest;
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
        adapter = new AccessManagementPortAdapter(replaceAccessUseCase, revokeSessionsByScopeUseCase, idGenerator);
    }

    @Test
    void grantAccess_validRequest_executesReplaceAccessUseCase() {
        Id userId = () -> "u-123";
        when(idGenerator.convertIdFrom("u-123")).thenReturn(userId);

        adapter.grantAccess(new GrantAccessRequest("u-123", "MERCHANT", "ADMIN", "STORE", "s-1"));

        ArgumentCaptor<ReplaceAccessCommand> captor = ArgumentCaptor.forClass(ReplaceAccessCommand.class);
        verify(replaceAccessUseCase).execute(captor.capture());

        ReplaceAccessCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.platformCode()).isEqualTo("MERCHANT");
        assertThat(command.replacementRoleCode()).isEqualTo("ADMIN");
        assertThat(command.scopeKey()).isEqualTo("STORE");
        assertThat(command.scopeId()).isEqualTo("s-1");
    }

    @Test
    void revokeAccess_validRequest_executesReplaceAccessUseCaseWithNullRole() {
        Id userId = () -> "u-123";
        when(idGenerator.convertIdFrom("u-123")).thenReturn(userId);

        adapter.revokeAccess(new RevokeAccessRequest("u-123", "MERCHANT", "STORE", "s-1"));

        ArgumentCaptor<ReplaceAccessCommand> captor = ArgumentCaptor.forClass(ReplaceAccessCommand.class);
        verify(replaceAccessUseCase).execute(captor.capture());

        ReplaceAccessCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.platformCode()).isEqualTo("MERCHANT");
        assertThat(command.replacementRoleCode()).isNull();
        assertThat(command.scopeKey()).isEqualTo("STORE");
        assertThat(command.scopeId()).isEqualTo("s-1");
    }

    @Test
    void revokeSessionsByScope_validScope_executesRevokeSessionsByScopeUseCase() {
        adapter.revokeSessionsByScope("MERCHANT", "STORE", "s-1");

        ArgumentCaptor<RevokeSessionsByScopeCommand> captor = ArgumentCaptor.forClass(RevokeSessionsByScopeCommand.class);
        verify(revokeSessionsByScopeUseCase).execute(captor.capture());

        RevokeSessionsByScopeCommand command = captor.getValue();
        assertThat(command.platformCode()).isEqualTo("MERCHANT");
        assertThat(command.scopeKey()).isEqualTo("STORE");
        assertThat(command.scopeId()).isEqualTo("s-1");
    }
}
