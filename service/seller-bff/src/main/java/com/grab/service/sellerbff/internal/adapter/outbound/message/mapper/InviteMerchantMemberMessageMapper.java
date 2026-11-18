package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantMembershipPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.InviteMemberRecord;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InviteMerchantMemberMessageMapper {

    @Mapping(target = "merchantId", source = "record.merchantId")
    @Mapping(target = "actorId", source = "record.actorId")
    @Mapping(target = "targetUserId", source = "record.targetUserId")
    @Mapping(target = "role", source = "record.role")
    @Mapping(target = "expiresAt", source = "record.expiresAt")
    public abstract MerchantMembershipPort.InviteMemberMessage toMessage(InviteMemberRecord record);

    public abstract MerchantMemberOutboundResult toResult(MerchantMembershipPort.MemberMessageResult result);
}

