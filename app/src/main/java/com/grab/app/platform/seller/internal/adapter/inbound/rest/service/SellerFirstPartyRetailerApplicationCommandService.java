package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartSellerMerchantApplicationMapper;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.StartSellerMerchantApplicationCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerFirstPartyRetailerApplicationCommandService {

    private static final String FIRST_PARTY_BUSINESS_TYPE = "FIRST_PARTY_RETAILER";

    private final CommandBus commandBus;
    private final StartSellerMerchantApplicationMapper mapper;

    public SellerFirstPartyRetailerApplicationCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            StartSellerMerchantApplicationMapper mapper
    ) {
        this.commandBus = commandBus;
        this.mapper = mapper;
    }

    public MerchantResponse startFirstPartyApplication(String applicantUserId, StartMerchantApplicationRequest request) {
        StartSellerMerchantApplicationCommand command = new StartSellerMerchantApplicationCommand(
                applicantUserId,
                FIRST_PARTY_BUSINESS_TYPE,
                request.displayName(),
                request.primaryMarketCountryCode()
        );
        MerchantOutboundResult result = commandBus.dispatch(command);
        return mapper.toResponse(result);
    }
}

