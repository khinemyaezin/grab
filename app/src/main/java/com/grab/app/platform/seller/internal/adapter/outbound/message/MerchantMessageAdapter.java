package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.account.merchant.port.message.MerchantAccountPort;
import com.grab.app.account.merchant.port.message.MerchantMembershipPort;
import com.grab.app.account.merchant.port.message.MerchantStorefrontPort;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.AcceptMerchantInvitationMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreateStorefrontMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetMerchantMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetStorefrontMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.InviteMerchantMemberMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListMerchantMembersMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListMyMerchantsMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListStorefrontsMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.StartMerchantApplicationMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.SubmitMerchantApplicationMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.UpdateMerchantMemberRoleMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.UpdateMerchantProfileMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.UpdateStorefrontMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MerchantMessageAdapter implements SellerMerchantOutboundPort {

    private final MerchantAccountPort accountPort;
    private final MerchantStorefrontPort storefrontPort;
    private final MerchantMembershipPort membershipPort;
    private final IdGenerator idGenerator;

    private final StartMerchantApplicationMessageMapper startApplicationMapper;
    private final SubmitMerchantApplicationMessageMapper submitApplicationMapper;
    private final GetMerchantMessageMapper getMerchantMapper;
    private final ListMyMerchantsMessageMapper listMyMerchantsMapper;
    private final UpdateMerchantProfileMessageMapper updateProfileMapper;
    private final CreateStorefrontMessageMapper createStorefrontMapper;
    private final GetStorefrontMessageMapper getStorefrontMapper;
    private final ListStorefrontsMessageMapper listStorefrontsMapper;
    private final UpdateStorefrontMessageMapper updateStorefrontMapper;
    private final InviteMerchantMemberMessageMapper inviteMemberMapper;
    private final AcceptMerchantInvitationMessageMapper acceptInvitationMapper;
    private final UpdateMerchantMemberRoleMessageMapper updateRoleMapper;
    private final ListMerchantMembersMessageMapper listMembersMapper;

    public MerchantMessageAdapter(
            MerchantAccountPort accountPort,
            MerchantStorefrontPort storefrontPort,
            MerchantMembershipPort membershipPort,
            IdGenerator idGenerator,
            StartMerchantApplicationMessageMapper startApplicationMapper,
            SubmitMerchantApplicationMessageMapper submitApplicationMapper,
            GetMerchantMessageMapper getMerchantMapper,
            ListMyMerchantsMessageMapper listMyMerchantsMapper,
            UpdateMerchantProfileMessageMapper updateProfileMapper,
            CreateStorefrontMessageMapper createStorefrontMapper,
            GetStorefrontMessageMapper getStorefrontMapper,
            ListStorefrontsMessageMapper listStorefrontsMapper,
            UpdateStorefrontMessageMapper updateStorefrontMapper,
            InviteMerchantMemberMessageMapper inviteMemberMapper,
            AcceptMerchantInvitationMessageMapper acceptInvitationMapper,
            UpdateMerchantMemberRoleMessageMapper updateRoleMapper,
            ListMerchantMembersMessageMapper listMembersMapper
    ) {
        this.accountPort = accountPort;
        this.storefrontPort = storefrontPort;
        this.membershipPort = membershipPort;
        this.idGenerator = idGenerator;
        this.startApplicationMapper = startApplicationMapper;
        this.submitApplicationMapper = submitApplicationMapper;
        this.getMerchantMapper = getMerchantMapper;
        this.listMyMerchantsMapper = listMyMerchantsMapper;
        this.updateProfileMapper = updateProfileMapper;
        this.createStorefrontMapper = createStorefrontMapper;
        this.getStorefrontMapper = getStorefrontMapper;
        this.listStorefrontsMapper = listStorefrontsMapper;
        this.updateStorefrontMapper = updateStorefrontMapper;
        this.inviteMemberMapper = inviteMemberMapper;
        this.acceptInvitationMapper = acceptInvitationMapper;
        this.updateRoleMapper = updateRoleMapper;
        this.listMembersMapper = listMembersMapper;
    }

    @Override
    public MerchantOutboundResult startApplication(StartApplicationRecord record) {
        MerchantAccountPort.StartMerchantApplicationMessage message = startApplicationMapper.toMessage(record);
        MerchantAccountPort.MerchantMessageResult result = accountPort.startApplication(message);
        return startApplicationMapper.toResult(result);
    }

    @Override
    public MerchantOutboundResult submitApplication(String applicationIdStr, String actorIdStr) {
        Id applicationId = idGenerator.convertIdFrom(applicationIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantAccountPort.MerchantMessageResult result = accountPort.submitApplication(applicationId, actorId);
        return submitApplicationMapper.toResult(result);
    }

    @Override
    public Optional<MerchantOutboundResult> findMerchantById(String merchantIdStr, String actorIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        return accountPort.findMerchantById(merchantId, actorId, false, true).map(getMerchantMapper::toResult);
    }

    @Override
    public List<MerchantOutboundResult> listMyMerchants(String actorIdStr) {
        Id userId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        List<MerchantAccountPort.MerchantMessageResult> results = accountPort.listMyMerchants(userId);
        return listMyMerchantsMapper.toResultList(results);
    }

    @Override
    public MerchantOutboundResult updateProfile(UpdateProfileRecord record) {
        MerchantAccountPort.UpdateMerchantProfileMessage message = updateProfileMapper.toMessage(record);
        MerchantAccountPort.MerchantMessageResult result = accountPort.updateProfile(message);
        return updateProfileMapper.toResult(result);
    }

    @Override
    public StorefrontOutboundResult createStorefront(CreateStorefrontRecord record) {
        MerchantStorefrontPort.CreateStorefrontMessage message = createStorefrontMapper.toMessage(record);
        MerchantStorefrontPort.StorefrontMessageResult result = storefrontPort.createStorefront(message);
        return createStorefrontMapper.toResult(result);
    }

    @Override
    public Optional<StorefrontOutboundResult> findStorefrontById(String merchantIdStr, String storefrontIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id storefrontId = idGenerator.convertIdFrom(storefrontIdStr);
        return storefrontPort.findStorefrontById(storefrontId, merchantId).map(getStorefrontMapper::toResult);
    }

    @Override
    public List<StorefrontOutboundResult> listStorefronts(String merchantIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        List<MerchantStorefrontPort.StorefrontMessageResult> results = storefrontPort.listStorefrontsByMerchant(merchantId);
        return listStorefrontsMapper.toResultList(results);
    }

    @Override
    public StorefrontOutboundResult updateStorefront(UpdateStorefrontRecord record) {
        MerchantStorefrontPort.UpdateStorefrontMessage message = updateStorefrontMapper.toMessage(record);
        MerchantStorefrontPort.StorefrontMessageResult result = storefrontPort.updateStorefront(
                idGenerator.convertIdFrom(record.storefrontId()),
                idGenerator.convertIdFrom(record.merchantId()),
                message
        );
        return updateStorefrontMapper.toResult(result);
    }

    @Override
    public void activateStorefront(String merchantIdStr, String storefrontIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id storefrontId = idGenerator.convertIdFrom(storefrontIdStr);
        storefrontPort.activateStorefront(storefrontId, merchantId);
    }

    @Override
    public void closeStorefront(String merchantIdStr, String storefrontIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id storefrontId = idGenerator.convertIdFrom(storefrontIdStr);
        storefrontPort.closeStorefront(storefrontId, merchantId);
    }

    @Override
    public MerchantMemberOutboundResult inviteMember(InviteMemberRecord record) {
        MerchantMembershipPort.InviteMemberMessage message = inviteMemberMapper.toMessage(record);
        MerchantMembershipPort.MemberMessageResult result = membershipPort.inviteMember(message);
        return inviteMemberMapper.toResult(result);
    }

    @Override
    public MerchantMemberOutboundResult acceptInvitation(String merchantIdStr, String memberIdStr, String actorIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id memberId = idGenerator.convertIdFrom(memberIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantMembershipPort.MemberMessageResult result = membershipPort.acceptInvitation(merchantId, memberId, actorId);
        return acceptInvitationMapper.toResult(result);
    }

    @Override
    public MerchantMemberOutboundResult updateMemberRole(UpdateMemberRoleRecord record) {
        Id merchantId = idGenerator.convertIdFrom(record.merchantId());
        Id memberId = idGenerator.convertIdFrom(record.memberId());
        Id actorId = (record.actorId() != null && !record.actorId().isBlank()) ? idGenerator.convertIdFrom(record.actorId()) : null;
        MerchantMembershipPort.MemberMessageResult result = membershipPort.updateMemberRole(merchantId, memberId, actorId, record.newRole());
        return updateRoleMapper.toResult(result);
    }

    @Override
    public void removeMember(String merchantIdStr, String memberIdStr, String actorIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id memberId = idGenerator.convertIdFrom(memberIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        membershipPort.removeMember(merchantId, memberId, actorId);
    }

    @Override
    public List<MerchantMemberOutboundResult> listMembers(String merchantIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        List<MerchantMembershipPort.MemberMessageResult> results = membershipPort.listMembers(merchantId);
        return listMembersMapper.toResultList(results);
    }

    @Override
    public MerchantOutboundResult approveMerchant(String merchantIdStr, String actorIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantAccountPort.MerchantMessageResult result = accountPort.approveMerchant(merchantId, actorId);
        return getMerchantMapper.toResult(result);
    }

    @Override
    public MerchantOutboundResult rejectMerchant(String merchantIdStr, String actorIdStr, String reason) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantAccountPort.MerchantMessageResult result = accountPort.rejectMerchant(merchantId, actorId, reason);
        return getMerchantMapper.toResult(result);
    }

    @Override
    public MerchantOutboundResult requestChanges(String merchantIdStr, String actorIdStr, String comments) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantAccountPort.MerchantMessageResult result = accountPort.requestChanges(merchantId, actorId, comments);
        return getMerchantMapper.toResult(result);
    }

    @Override
    public MerchantOutboundResult suspendMerchant(String merchantIdStr, String actorIdStr, String reason) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantAccountPort.MerchantMessageResult result = accountPort.suspendMerchant(merchantId, actorId, reason);
        return getMerchantMapper.toResult(result);
    }

    @Override
    public MerchantOutboundResult reactivateMerchant(String merchantIdStr, String actorIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantAccountPort.MerchantMessageResult result = accountPort.reactivateMerchant(merchantId, actorId);
        return getMerchantMapper.toResult(result);
    }

    @Override
    public MerchantOutboundResult closeMerchant(String merchantIdStr, String actorIdStr) {
        Id merchantId = idGenerator.convertIdFrom(merchantIdStr);
        Id actorId = (actorIdStr != null && !actorIdStr.isBlank()) ? idGenerator.convertIdFrom(actorIdStr) : null;
        MerchantAccountPort.MerchantMessageResult result = accountPort.closeMerchant(merchantId, actorId);
        return getMerchantMapper.toResult(result);
    }

    @Override
    public List<MerchantOutboundResult> listReviewQueue(String status) {
        List<MerchantAccountPort.MerchantMessageResult> results = accountPort.listReviewQueue(status);
        return results.stream().map(getMerchantMapper::toResult).toList();
    }
}
