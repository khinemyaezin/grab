package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.merchant.internal.api.rest.dto.request.ChangeMerchantMemberRoleRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.InviteMerchantMemberRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.grab.store.merchant.internal.api.rest.mapper.*;
import com.merchant.application.model.write.*;
import com.merchant.domain.valueobject.MerchantRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MerchantMemberCommandServiceTest {

    private CommandBus commands;
    private InviteMerchantMemberRequestMapper inviteMapper;
    private AcceptMerchantMemberInvitationRequestMapper acceptMapper;
    private ChangeMerchantMemberRoleRequestMapper changeRoleMapper;
    private RemoveMerchantMemberRequestMapper removeMapper;
    private MerchantMemberCommandService service;

    private final Instant now = Instant.parse("2026-09-23T10:00:00Z");

    @BeforeEach
    void setUp() {
        commands = mock(CommandBus.class);
        inviteMapper = mock(InviteMerchantMemberRequestMapper.class);
        acceptMapper = mock(AcceptMerchantMemberInvitationRequestMapper.class);
        changeRoleMapper = mock(ChangeMerchantMemberRoleRequestMapper.class);
        removeMapper = mock(RemoveMerchantMemberRequestMapper.class);

        service = new MerchantMemberCommandService(
                commands, inviteMapper, acceptMapper, changeRoleMapper, removeMapper
        );
    }

    @Test
    void invite_shouldMapAndDispatchCommand() {
        InviteMerchantMemberRequest request = new InviteMerchantMemberRequest("usr-target", "OPERATOR", now.plusSeconds(3600));
        InviteMerchantMemberCommand command = new InviteMerchantMemberCommand(
                new CommonId("mer-1"), new CommonId("usr-actor"), new CommonId("usr-target"),
                MerchantRole.of("OPERATOR"), now.plusSeconds(3600)
        );
        MerchantMemberResult result = sampleResult("mem-1", "OPERATOR", "INVITED");
        MerchantMemberResponse response = sampleResponse("mem-1", "OPERATOR", "INVITED");

        when(inviteMapper.toCommand(eq("mer-1"), eq("usr-actor"), eq(request), any())).thenReturn(command);
        when(commands.dispatch(command)).thenReturn(result);
        when(inviteMapper.toResponse(result)).thenReturn(response);

        MerchantMemberResponse actual = service.invite("mer-1", request, "usr-actor");

        assertThat(actual).isNotNull();
        assertThat(actual.memberId()).isEqualTo("mem-1");
        verify(commands).dispatch(command);
    }

    @Test
    void accept_shouldMapAndDispatchCommand() {
        AcceptMerchantMemberInvitationCommand command = new AcceptMerchantMemberInvitationCommand(
                new CommonId("mer-1"), new CommonId("mem-1"), new CommonId("usr-actor")
        );
        MerchantMemberResult result = sampleResult("mem-1", "OPERATOR", "ACTIVE");
        MerchantMemberResponse response = sampleResponse("mem-1", "OPERATOR", "ACTIVE");

        when(acceptMapper.toCommand("mer-1", "mem-1", "usr-actor")).thenReturn(command);
        when(commands.dispatch(command)).thenReturn(result);
        when(acceptMapper.toResponse(result)).thenReturn(response);

        MerchantMemberResponse actual = service.accept("mer-1", "mem-1", "usr-actor");

        assertThat(actual.status()).isEqualTo("ACTIVE");
        verify(commands).dispatch(command);
    }

    @Test
    void changeRole_shouldMapAndDispatchCommand() {
        ChangeMerchantMemberRoleRequest request = new ChangeMerchantMemberRoleRequest("SUPERVISOR");
        ChangeMerchantMemberRoleCommand command = new ChangeMerchantMemberRoleCommand(
                new CommonId("mer-1"), new CommonId("mem-1"), new CommonId("usr-actor"), MerchantRole.of("SUPERVISOR")
        );
        MerchantMemberResult result = sampleResult("mem-1", "SUPERVISOR", "ACTIVE");
        MerchantMemberResponse response = sampleResponse("mem-1", "SUPERVISOR", "ACTIVE");

        when(changeRoleMapper.toCommand("mer-1", "mem-1", "usr-actor", request)).thenReturn(command);
        when(commands.dispatch(command)).thenReturn(result);
        when(changeRoleMapper.toResponse(result)).thenReturn(response);

        MerchantMemberResponse actual = service.changeRole("mer-1", "mem-1", request, "usr-actor");

        assertThat(actual.role()).isEqualTo("SUPERVISOR");
        verify(commands).dispatch(command);
    }

    @Test
    void remove_shouldMapAndDispatchCommand() {
        RemoveMerchantMemberCommand command = new RemoveMerchantMemberCommand(
                new CommonId("mer-1"), new CommonId("mem-1"), new CommonId("usr-actor")
        );
        MerchantMemberResult result = sampleResult("mem-1", "OPERATOR", "REMOVED");
        MerchantMemberResponse response = sampleResponse("mem-1", "OPERATOR", "REMOVED");

        when(removeMapper.toCommand("mer-1", "mem-1", "usr-actor")).thenReturn(command);
        when(commands.dispatch(command)).thenReturn(result);
        when(removeMapper.toResponse(result)).thenReturn(response);

        MerchantMemberResponse actual = service.remove("mer-1", "mem-1", "usr-actor");

        assertThat(actual.status()).isEqualTo("REMOVED");
        verify(commands).dispatch(command);
    }

    private MerchantMemberResult sampleResult(String memberId, String role, String status) {
        return new MerchantMemberResult(
                memberId, "mer-1", "usr-1", role, status, null, null, now, now, now, 0L
        );
    }

    private MerchantMemberResponse sampleResponse(String memberId, String role, String status) {
        return new MerchantMemberResponse(
                memberId, "mer-1", "usr-1", role, status, null, null, now, now, now, 0L
        );
    }
}
