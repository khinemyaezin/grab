package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.StartSellerMerchantApplicationCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class StartSellerMerchantApplicationMapper {

    @Mapping(target = "actorId", source = "applicantUserId")
    @Mapping(target = "businessType", source = "request.businessType")
    @Mapping(target = "displayName", source = "request.displayName")
    @Mapping(target = "primaryMarketCountryCode", source = "request.primaryMarketCountryCode")
    public abstract StartSellerMerchantApplicationCommand toCommand(String applicantUserId, StartMerchantApplicationRequest request);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}
