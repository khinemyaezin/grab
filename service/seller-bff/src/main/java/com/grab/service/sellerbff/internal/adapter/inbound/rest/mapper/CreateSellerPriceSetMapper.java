package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import com.grab.service.sellerbff.internal.command.model.SellerPricingCommands.CreateSellerPriceSetCommand;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerPriceSetMapper {

    public CreateSellerPriceSetCommand toCommand() {
        return new CreateSellerPriceSetCommand();
    }

    public abstract PriceSetResponse toResponse(PriceSetOutboundResult result);
}

