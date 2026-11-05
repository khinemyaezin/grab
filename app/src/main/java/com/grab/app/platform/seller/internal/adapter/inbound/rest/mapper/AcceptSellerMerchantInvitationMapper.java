package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.AcceptSellerMerchantInvitationCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class AcceptSellerMerchantInvitationMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "actorId", source = "actorUserId")
    public abstract AcceptSellerMerchantInvitationCommand toCommand(String merchantId, String memberId, String actorUserId);

    public abstract MerchantMemberResponse toResponse(MerchantMemberOutboundResult result);
}
