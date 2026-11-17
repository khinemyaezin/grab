package com.grab.service.account.merchant.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantMembershipPort.MemberMessageResult;
import com.grab.service.account.internal.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RemoveMerchantMemberPortMapper {

    public RemoveMerchantMemberCommand toCommand(Id merchantId, Id memberId, Id actorId) {
        return new RemoveMerchantMemberCommand(merchantId, memberId, actorId);
    }

    public abstract MemberMessageResult toResponse(MerchantMemberResult result);
}
