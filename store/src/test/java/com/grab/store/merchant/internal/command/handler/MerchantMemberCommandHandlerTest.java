package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.*;
import com.merchant.application.port.inbound.*;
import com.merchant.domain.valueobject.MerchantRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MerchantMemberCommandHandlerTest {

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @Test
    void inviteCommandHandler_shouldDelegateToUseCase() {
        InviteMerchantMemberUseCase useCase = mock(InviteMerchantMemberUseCase.class);
        InviteMerchantMemberCommandHandler handler = new InviteMerchantMemberCommandHandler(useCase);

        InviteMerchantMemberCommand command = new InviteMerchantMemberCommand(
                new CommonId("mer-1"), new CommonId("usr-actor"), new CommonId("usr-target"),
                MerchantRole.of("OPERATOR"), now.plusSeconds(3600)
        );
        MerchantMemberResult expected = sampleResult("mem-1", "OPERATOR", "INVITED");
        when(useCase.execute(command)).thenReturn(expected);

        MerchantMemberResult actual = handler.handle(command);

        assertThat(actual).isEqualTo(expected);
        assertThat(handler.getCommandType()).isEqualTo(InviteMerchantMemberCommand.class);
        verify(useCase).execute(command);
    }

    @Test
    void acceptCommandHandler_shouldDelegateToUseCase() {
        AcceptMerchantMemberInvitationUseCase useCase = mock(AcceptMerchantMemberInvitationUseCase.class);
        AcceptMerchantMemberInvitationCommandHandler handler = new AcceptMerchantMemberInvitationCommandHandler(useCase);

        AcceptMerchantMemberInvitationCommand command = new AcceptMerchantMemberInvitationCommand(
                new CommonId("mer-1"), new CommonId("mem-1"), new CommonId("usr-actor")
        );
        MerchantMemberResult expected = sampleResult("mem-1", "OPERATOR", "ACTIVE");
        when(useCase.execute(command)).thenReturn(expected);

        MerchantMemberResult actual = handler.handle(command);

        assertThat(actual).isEqualTo(expected);
        assertThat(handler.getCommandType()).isEqualTo(AcceptMerchantMemberInvitationCommand.class);
        verify(useCase).execute(command);
    }

    @Test
    void changeRoleCommandHandler_shouldDelegateToUseCase() {
        ChangeMerchantMemberRoleUseCase useCase = mock(ChangeMerchantMemberRoleUseCase.class);
        ChangeMerchantMemberRoleCommandHandler handler = new ChangeMerchantMemberRoleCommandHandler(useCase);

        ChangeMerchantMemberRoleCommand command = new ChangeMerchantMemberRoleCommand(
                new CommonId("mer-1"), new CommonId("mem-1"), new CommonId("usr-actor"),
                MerchantRole.of("SUPERVISOR")
        );
        MerchantMemberResult expected = sampleResult("mem-1", "SUPERVISOR", "ACTIVE");
        when(useCase.execute(command)).thenReturn(expected);

        MerchantMemberResult actual = handler.handle(command);

        assertThat(actual).isEqualTo(expected);
        assertThat(handler.getCommandType()).isEqualTo(ChangeMerchantMemberRoleCommand.class);
        verify(useCase).execute(command);
    }

    @Test
    void removeCommandHandler_shouldDelegateToUseCase() {
        RemoveMerchantMemberUseCase useCase = mock(RemoveMerchantMemberUseCase.class);
        RemoveMerchantMemberCommandHandler handler = new RemoveMerchantMemberCommandHandler(useCase);

        RemoveMerchantMemberCommand command = new RemoveMerchantMemberCommand(
                new CommonId("mer-1"), new CommonId("mem-1"), new CommonId("usr-actor")
        );
        MerchantMemberResult expected = sampleResult("mem-1", "OPERATOR", "REMOVED");
        when(useCase.execute(command)).thenReturn(expected);

        MerchantMemberResult actual = handler.handle(command);

        assertThat(actual).isEqualTo(expected);
        assertThat(handler.getCommandType()).isEqualTo(RemoveMerchantMemberCommand.class);
        verify(useCase).execute(command);
    }

    @Test
    void provisionAdminCommandHandler_shouldDelegateToUseCase() {
        ProvisionMerchantAdminUseCase useCase = mock(ProvisionMerchantAdminUseCase.class);
        ProvisionMerchantAdminCommandHandler handler = new ProvisionMerchantAdminCommandHandler(useCase);

        ProvisionMerchantAdminCommand command = new ProvisionMerchantAdminCommand(
                new CommonId("mer-1"), new CommonId("usr-applicant"), now
        );
        MerchantMemberResult expected = sampleResult("mem-1", "MERCHANT_ADMIN", "ACTIVE");
        when(useCase.execute(command)).thenReturn(expected);

        MerchantMemberResult actual = handler.handle(command);

        assertThat(actual).isEqualTo(expected);
        assertThat(handler.getCommandType()).isEqualTo(ProvisionMerchantAdminCommand.class);
        verify(useCase).execute(command);
    }

    private MerchantMemberResult sampleResult(String memberId, String role, String status) {
        return new MerchantMemberResult(
                memberId, "mer-1", "usr-1", role, status, null, null, now, now, now, 0L
        );
    }
}
