package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantMembershipPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMerchantMembersMessageMapper {

    public abstract List<MerchantMemberOutboundResult> toResultList(List<MerchantMembershipPort.MemberMessageResult> results);

    public abstract MerchantMemberOutboundResult toResult(MerchantMembershipPort.MemberMessageResult result);
}

