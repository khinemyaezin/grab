package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.CreatePriceListRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.service.sellerbff.internal.command.model.SellerPricingCommands.CreateSellerPriceListCommand;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerPriceListMapper {

    public abstract CreateSellerPriceListCommand toCommand(CreatePriceListRequest request);

    public abstract PriceListResponse toResponse(PriceListOutboundResult result);
}

