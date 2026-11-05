package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.InviteMemberRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.InviteSellerMerchantMemberCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class InviteSellerMerchantMemberMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "targetUserId", source = "request.targetUserId")
    @Mapping(target = "role", source = "request.role")
    @Mapping(target = "expiresAt", source = "request.expiresAt")
    public abstract InviteSellerMerchantMemberCommand toCommand(String merchantId, String actorId, InviteMemberRequest request);

    public abstract MerchantMemberResponse toResponse(MerchantMemberOutboundResult result);
}

