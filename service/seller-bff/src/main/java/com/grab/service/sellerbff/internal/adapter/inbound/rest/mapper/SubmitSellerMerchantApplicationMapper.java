package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.SubmitSellerMerchantApplicationCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class SubmitSellerMerchantApplicationMapper {

    @Mapping(target = "applicationId", source = "merchantId")
    @Mapping(target = "actorId", source = "applicantUserId")
    public abstract SubmitSellerMerchantApplicationCommand toCommand(String merchantId, String applicantUserId);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}
