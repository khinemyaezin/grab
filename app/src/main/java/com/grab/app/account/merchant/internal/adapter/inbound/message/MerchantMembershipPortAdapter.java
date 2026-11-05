package com.grab.app.account.merchant.internal.adapter.inbound.message;

import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.AcceptMerchantMemberInvitationPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.ChangeMerchantMemberRolePortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.InviteMerchantMemberPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.ListMerchantMembersPortMapper;
import com.grab.app.account.merchant.internal.adapter.inbound.message.mapper.RemoveMerchantMemberPortMapper;
import com.grab.app.account.merchant.port.message.MerchantMembershipPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.merchant.application.model.read.ListMerchantMembersQuery;
import com.merchant.application.model.write.AcceptMerchantMemberInvitationCommand;
import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MerchantMembershipPortAdapter implements MerchantMembershipPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final InviteMerchantMemberPortMapper inviteMemberMapper;
    private final AcceptMerchantMemberInvitationPortMapper acceptInvitationMapper;
    private final ChangeMerchantMemberRolePortMapper changeRoleMapper;
    private final RemoveMerchantMemberPortMapper removeMemberMapper;
    private final ListMerchantMembersPortMapper listMembersMapper;

    public MerchantMembershipPortAdapter(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("accountQueryBus") QueryBus queryBus,
            InviteMerchantMemberPortMapper inviteMemberMapper,
            AcceptMerchantMemberInvitationPortMapper acceptInvitationMapper,
            ChangeMerchantMemberRolePortMapper changeRoleMapper,
            RemoveMerchantMemberPortMapper removeMemberMapper,
            ListMerchantMembersPortMapper listMembersMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.inviteMemberMapper = inviteMemberMapper;
        this.acceptInvitationMapper = acceptInvitationMapper;
        this.changeRoleMapper = changeRoleMapper;
        this.removeMemberMapper = removeMemberMapper;
        this.listMembersMapper = listMembersMapper;
    }

    @Override
    public MemberMessageResult inviteMember(InviteMemberMessage message) {
        InviteMerchantMemberCommand command = inviteMemberMapper.toCommand(message);
        MerchantMemberResult result = commandBus.dispatch(command);
        return inviteMemberMapper.toResponse(result);
    }

    @Override
    public MemberMessageResult acceptInvitation(Id merchantId, Id memberId, Id actorUserId) {
        AcceptMerchantMemberInvitationCommand command = acceptInvitationMapper.toCommand(merchantId, memberId, actorUserId);
        MerchantMemberResult result = commandBus.dispatch(command);
        return acceptInvitationMapper.toResponse(result);
    }

    @Override
    public MemberMessageResult updateMemberRole(Id merchantId, Id memberId, Id actorId, String newRole) {
        ChangeMerchantMemberRoleCommand command = changeRoleMapper.toCommand(merchantId, memberId, actorId, newRole);
        MerchantMemberResult result = commandBus.dispatch(command);
        return changeRoleMapper.toResponse(result);
    }

    @Override
    public MemberMessageResult removeMember(Id merchantId, Id memberId, Id actorId) {
        RemoveMerchantMemberCommand command = removeMemberMapper.toCommand(merchantId, memberId, actorId);
        MerchantMemberResult result = commandBus.dispatch(command);
        return removeMemberMapper.toResponse(result);
    }

    @Override
    public List<MemberMessageResult> listMembers(Id merchantId) {
        ListMerchantMembersQuery query = listMembersMapper.toQuery(merchantId);
        List<MerchantMemberResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(listMembersMapper::toResponse).toList();
    }
}
