package com.grab.store.merchant.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.merchant.internal.api.rest.dto.request.ChangeMerchantMemberRoleRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.InviteMerchantMemberRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantMemberResponse;
import com.grab.store.merchant.internal.api.rest.mapper.AcceptMerchantMemberInvitationRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.ChangeMerchantMemberRoleRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.InviteMerchantMemberRequestMapper;
import com.grab.store.merchant.internal.api.rest.mapper.RemoveMerchantMemberRequestMapper;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.merchant.application.model.write.AcceptMerchantMemberInvitationCommand;
import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantMemberCommandService {
    private final CommandBus commands;
    private final InviteMerchantMemberRequestMapper inviteMapper;
    private final AcceptMerchantMemberInvitationRequestMapper acceptMapper;
    private final ChangeMerchantMemberRoleRequestMapper changeRoleMapper;
    private final RemoveMerchantMemberRequestMapper removeMapper;

    public MerchantMemberResponse invite(
            String merchantId, InviteMerchantMemberRequest request, String actorId
    ) {
        Instant expiresAt = request.expiresAt() != null ? request.expiresAt() : Instant.now().plusSeconds(86400 * 7);
        InviteMerchantMemberCommand command = inviteMapper.toCommand(merchantId, actorId, request, expiresAt);
        MerchantMemberResult result = commands.dispatch(command);
        return inviteMapper.toResponse(result);
    }

    public MerchantMemberResponse accept(String merchantId, String memberId, String actorId) {
        AcceptMerchantMemberInvitationCommand command = acceptMapper.toCommand(merchantId, memberId, actorId);
        MerchantMemberResult result = commands.dispatch(command);
        return acceptMapper.toResponse(result);
    }

    public MerchantMemberResponse changeRole(
            String merchantId, String memberId, ChangeMerchantMemberRoleRequest request, String actorId
    ) {
        ChangeMerchantMemberRoleCommand command = changeRoleMapper.toCommand(merchantId, memberId, actorId, request);
        MerchantMemberResult result = commands.dispatch(command);
        return changeRoleMapper.toResponse(result);
    }

    public MerchantMemberResponse remove(String merchantId, String memberId, String actorId) {
        RemoveMerchantMemberCommand command = removeMapper.toCommand(merchantId, memberId, actorId);
        MerchantMemberResult result = commands.dispatch(command);
        return removeMapper.toResponse(result);
    }
}
