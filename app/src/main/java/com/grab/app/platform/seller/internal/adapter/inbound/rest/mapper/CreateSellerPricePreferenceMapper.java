package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPricePreferenceCommand;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerPricePreferenceMapper {

    public abstract CreateSellerPricePreferenceCommand toCommand(CreatePricePreferenceRequest request);

    public abstract PricePreferenceResponse toResponse(PricePreferenceOutboundResult result);
}

