package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePriceListRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerPriceListMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerPricePreferenceMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerPriceSetMapper;
import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPriceListCommand;
import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPricePreferenceCommand;
import com.grab.app.platform.seller.internal.command.model.SellerPricingCommands.CreateSellerPriceSetCommand;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerPricingCommandService {

    private final CommandBus commandBus;
    private final CreateSellerPriceListMapper createPriceListMapper;
    private final CreateSellerPriceSetMapper createPriceSetMapper;
    private final CreateSellerPricePreferenceMapper createPricePreferenceMapper;

    public SellerPricingCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            CreateSellerPriceListMapper createPriceListMapper,
            CreateSellerPriceSetMapper createPriceSetMapper,
            CreateSellerPricePreferenceMapper createPricePreferenceMapper
    ) {
        this.commandBus = commandBus;
        this.createPriceListMapper = createPriceListMapper;
        this.createPriceSetMapper = createPriceSetMapper;
        this.createPricePreferenceMapper = createPricePreferenceMapper;
    }

    public PriceListResponse createPriceList(CreatePriceListRequest request) {
        CreateSellerPriceListCommand cmd = createPriceListMapper.toCommand(request);
        PriceListOutboundResult result = commandBus.dispatch(cmd);
        return createPriceListMapper.toResponse(result);
    }

    public PriceSetResponse createPriceSet() {
        CreateSellerPriceSetCommand cmd = createPriceSetMapper.toCommand();
        PriceSetOutboundResult result = commandBus.dispatch(cmd);
        return createPriceSetMapper.toResponse(result);
    }

    public PricePreferenceResponse createPricePreference(CreatePricePreferenceRequest request) {
        CreateSellerPricePreferenceCommand cmd = createPricePreferenceMapper.toCommand(request);
        PricePreferenceOutboundResult result = commandBus.dispatch(cmd);
        return createPricePreferenceMapper.toResponse(result);
    }
}
