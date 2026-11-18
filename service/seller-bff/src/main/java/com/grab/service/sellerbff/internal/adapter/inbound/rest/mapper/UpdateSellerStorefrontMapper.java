package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.UpdateStorefrontRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.service.sellerbff.internal.command.model.SellerMerchantCommands.UpdateSellerStorefrontCommand;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class UpdateSellerStorefrontMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "storefrontId", source = "storefrontId")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "slug", source = "request.slug")
    public abstract UpdateSellerStorefrontCommand toCommand(String merchantId, String storefrontId, UpdateStorefrontRequest request);

    public abstract StorefrontResponse toResponse(StorefrontOutboundResult result);
}

