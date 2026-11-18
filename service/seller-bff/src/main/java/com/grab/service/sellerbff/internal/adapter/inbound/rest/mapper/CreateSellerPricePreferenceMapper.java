package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.service.sellerbff.internal.command.model.SellerPricingCommands.CreateSellerPricePreferenceCommand;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerPricePreferenceMapper {

    public abstract CreateSellerPricePreferenceCommand toCommand(CreatePricePreferenceRequest request);

    public abstract PricePreferenceResponse toResponse(PricePreferenceOutboundResult result);
}

