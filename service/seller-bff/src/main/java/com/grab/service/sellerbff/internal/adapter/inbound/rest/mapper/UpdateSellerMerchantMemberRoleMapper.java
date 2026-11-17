package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.UpdateMemberRoleRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.MerchantMemberResponse;
import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantMemberRoleCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantMemberOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
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

