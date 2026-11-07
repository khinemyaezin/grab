package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.SubmitSellerMerchantApplicationCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class SubmitSellerMerchantApplicationMapper {

    @Mapping(target = "applicationId", source = "merchantId")
    @Mapping(target = "actorId", source = "applicantUserId")
    public abstract SubmitSellerMerchantApplicationCommand toCommand(String merchantId, String applicantUserId);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}
