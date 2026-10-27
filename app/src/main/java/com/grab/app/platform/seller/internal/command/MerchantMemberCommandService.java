package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.ChangeMerchantMemberRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.InviteMerchantMemberRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.AcceptMerchantMemberInvitationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.ChangeMerchantMemberRoleRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.InviteMerchantMemberRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.mapper.RemoveMerchantMemberRequestMapper;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
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
