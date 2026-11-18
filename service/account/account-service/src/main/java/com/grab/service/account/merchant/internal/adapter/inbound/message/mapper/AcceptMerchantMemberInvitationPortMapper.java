package com.grab.service.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantMembershipPort.MemberMessageResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.AcceptMerchantMemberInvitationCommand;
import com.merchant.application.model.write.MerchantMemberResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AcceptMerchantMemberInvitationPortMapper {

    public AcceptMerchantMemberInvitationCommand toCommand(Id merchantId, Id memberId, Id actorUserId) {
        return new AcceptMerchantMemberInvitationCommand(merchantId, memberId, actorUserId);
    }

    public abstract MemberMessageResult toResponse(MerchantMemberResult result);
}
