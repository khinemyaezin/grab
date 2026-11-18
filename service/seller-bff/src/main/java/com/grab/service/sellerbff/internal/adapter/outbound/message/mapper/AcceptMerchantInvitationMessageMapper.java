package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantMembershipPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AcceptMerchantInvitationMessageMapper {

    public abstract MerchantMemberOutboundResult toResult(MerchantMembershipPort.MemberMessageResult result);
}

