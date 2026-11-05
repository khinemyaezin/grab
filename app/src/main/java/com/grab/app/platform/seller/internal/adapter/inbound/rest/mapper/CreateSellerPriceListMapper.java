package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePriceListRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPriceListCommand;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerPriceListMapper {

    public abstract CreateSellerPriceListCommand toCommand(CreatePriceListRequest request);

    public abstract PriceListResponse toResponse(PriceListOutboundResult result);
}

