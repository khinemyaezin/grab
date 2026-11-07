package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.CreateSellerStorefrontCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
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

