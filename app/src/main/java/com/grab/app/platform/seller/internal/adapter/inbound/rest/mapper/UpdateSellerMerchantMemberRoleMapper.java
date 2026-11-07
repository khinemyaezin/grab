package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMemberRoleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantMemberRoleCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class UpdateSellerMerchantMemberRoleMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "newRole", expression = "java(request.resolveRole())")
    public abstract UpdateSellerMerchantMemberRoleCommand toCommand(String merchantId, String memberId, String actorId, UpdateMemberRoleRequest request);

    public abstract MerchantMemberResponse toResponse(MerchantMemberOutboundResult result);
}

