package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.InviteMemberRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMemberRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.AcceptSellerMerchantInvitationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ActivateSellerStorefrontMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CloseSellerStorefrontMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerStorefrontMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.InviteSellerMerchantMemberMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.RemoveSellerMerchantMemberMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartSellerMerchantApplicationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.SubmitSellerMerchantApplicationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.UpdateSellerMerchantMemberRoleMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.UpdateSellerMerchantProfileMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.UpdateSellerStorefrontMapper;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.AcceptSellerMerchantInvitationCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.ActivateSellerStorefrontCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.CloseSellerStorefrontCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.CreateSellerStorefrontCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.InviteSellerMerchantMemberCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.RemoveSellerMerchantMemberCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.StartSellerMerchantApplicationCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.SubmitSellerMerchantApplicationCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantMemberRoleCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantProfileCommand;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerStorefrontCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerMerchantCommandService {

    private final CommandBus commandBus;
    private final StartSellerMerchantApplicationMapper startApplicationMapper;
    private final SubmitSellerMerchantApplicationMapper submitApplicationMapper;
    private final UpdateSellerMerchantProfileMapper updateProfileMapper;
    private final CreateSellerStorefrontMapper createStorefrontMapper;
    private final UpdateSellerStorefrontMapper updateStorefrontMapper;
    private final ActivateSellerStorefrontMapper activateStorefrontMapper;
    private final CloseSellerStorefrontMapper closeStorefrontMapper;
    private final InviteSellerMerchantMemberMapper inviteMemberMapper;
    private final AcceptSellerMerchantInvitationMapper acceptInvitationMapper;
    private final UpdateSellerMerchantMemberRoleMapper updateMemberRoleMapper;
    private final RemoveSellerMerchantMemberMapper removeMemberMapper;

    public SellerMerchantCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            StartSellerMerchantApplicationMapper startApplicationMapper,
            SubmitSellerMerchantApplicationMapper submitApplicationMapper,
            UpdateSellerMerchantProfileMapper updateProfileMapper,
            CreateSellerStorefrontMapper createStorefrontMapper,
            UpdateSellerStorefrontMapper updateStorefrontMapper,
            ActivateSellerStorefrontMapper activateStorefrontMapper,
            CloseSellerStorefrontMapper closeStorefrontMapper,
            InviteSellerMerchantMemberMapper inviteMemberMapper,
            AcceptSellerMerchantInvitationMapper acceptInvitationMapper,
            UpdateSellerMerchantMemberRoleMapper updateMemberRoleMapper,
            RemoveSellerMerchantMemberMapper removeMemberMapper
    ) {
        this.commandBus = commandBus;
        this.startApplicationMapper = startApplicationMapper;
        this.submitApplicationMapper = submitApplicationMapper;
        this.updateProfileMapper = updateProfileMapper;
        this.createStorefrontMapper = createStorefrontMapper;
        this.updateStorefrontMapper = updateStorefrontMapper;
        this.activateStorefrontMapper = activateStorefrontMapper;
        this.closeStorefrontMapper = closeStorefrontMapper;
        this.inviteMemberMapper = inviteMemberMapper;
        this.acceptInvitationMapper = acceptInvitationMapper;
        this.updateMemberRoleMapper = updateMemberRoleMapper;
        this.removeMemberMapper = removeMemberMapper;
    }

    public MerchantResponse startApplication(String applicantUserIdStr, StartMerchantApplicationRequest request) {
        StartSellerMerchantApplicationCommand cmd = startApplicationMapper.toCommand(applicantUserIdStr, request);
        MerchantOutboundResult result = commandBus.dispatch(cmd);
        return startApplicationMapper.toResponse(result);
    }

    public MerchantResponse submitApplication(String merchantIdStr, String applicantUserIdStr) {
        SubmitSellerMerchantApplicationCommand cmd = submitApplicationMapper.toCommand(merchantIdStr, applicantUserIdStr);
        MerchantOutboundResult result = commandBus.dispatch(cmd);
        return submitApplicationMapper.toResponse(result);
    }

    public MerchantResponse updateProfile(String merchantIdStr, String actorIdStr, UpdateMerchantProfileRequest request) {
        UpdateSellerMerchantProfileCommand cmd = updateProfileMapper.toCommand(merchantIdStr, actorIdStr, request);
        MerchantOutboundResult result = commandBus.dispatch(cmd);
        return updateProfileMapper.toResponse(result);
    }

    public StorefrontResponse createStorefront(String merchantIdStr, CreateStorefrontRequest request) {
        CreateSellerStorefrontCommand cmd = createStorefrontMapper.toCommand(merchantIdStr, request);
        StorefrontOutboundResult result = commandBus.dispatch(cmd);
        return createStorefrontMapper.toResponse(result);
    }

    public StorefrontResponse updateStorefront(String merchantIdStr, String storefrontIdStr, UpdateStorefrontRequest request) {
        UpdateSellerStorefrontCommand cmd = updateStorefrontMapper.toCommand(merchantIdStr, storefrontIdStr, request);
        StorefrontOutboundResult result = commandBus.dispatch(cmd);
        return updateStorefrontMapper.toResponse(result);
    }

    public void activateStorefront(String merchantIdStr, String storefrontIdStr) {
        ActivateSellerStorefrontCommand cmd = activateStorefrontMapper.toCommand(merchantIdStr, storefrontIdStr);
        commandBus.dispatch(cmd);
    }

    public void closeStorefront(String merchantIdStr, String storefrontIdStr) {
        CloseSellerStorefrontCommand cmd = closeStorefrontMapper.toCommand(merchantIdStr, storefrontIdStr);
        commandBus.dispatch(cmd);
    }

    public MerchantMemberResponse inviteMember(String merchantIdStr, String actorIdStr, InviteMemberRequest request) {
        InviteSellerMerchantMemberCommand cmd = inviteMemberMapper.toCommand(merchantIdStr, actorIdStr, request);
        MerchantMemberOutboundResult result = commandBus.dispatch(cmd);
        return inviteMemberMapper.toResponse(result);
    }

    public MerchantMemberResponse acceptInvitation(String merchantIdStr, String memberIdStr, String actorUserIdStr) {
        AcceptSellerMerchantInvitationCommand cmd = acceptInvitationMapper.toCommand(merchantIdStr, memberIdStr, actorUserIdStr);
        MerchantMemberOutboundResult result = commandBus.dispatch(cmd);
        return acceptInvitationMapper.toResponse(result);
    }

    public MerchantMemberResponse updateMemberRole(String merchantIdStr, String memberIdStr, String actorIdStr, UpdateMemberRoleRequest request) {
        UpdateSellerMerchantMemberRoleCommand cmd = updateMemberRoleMapper.toCommand(merchantIdStr, memberIdStr, actorIdStr, request);
        MerchantMemberOutboundResult result = commandBus.dispatch(cmd);
        return updateMemberRoleMapper.toResponse(result);
    }

    public void removeMember(String merchantIdStr, String memberIdStr, String actorIdStr) {
        RemoveSellerMerchantMemberCommand cmd = removeMemberMapper.toCommand(merchantIdStr, memberIdStr, actorIdStr);
        commandBus.dispatch(cmd);
    }
}
