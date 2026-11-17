package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.CreateStorefrontRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.CreateSellerStorefrontCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerStorefrontMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "slug", source = "request.slug")
    public abstract CreateSellerStorefrontCommand toCommand(String merchantId, CreateStorefrontRequest request);

    public abstract StorefrontResponse toResponse(StorefrontOutboundResult result);
}

