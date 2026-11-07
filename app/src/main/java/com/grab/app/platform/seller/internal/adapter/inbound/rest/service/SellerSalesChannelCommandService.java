package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.ProvisionWebsiteChannelRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PublishableKeyResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelProvisionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerPublishableKeyMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ProvisionSellerSalesChannelMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.RevokeSellerPublishableKeyMapper;
import com.grab.app.platform.seller.internal.command.model.SellerSalesChannelCommands.CreateSellerPublishableKeyCommand;
import com.grab.app.platform.seller.internal.command.model.SellerSalesChannelCommands.ProvisionSellerMerchantWebsiteChannelCommand;
import com.grab.app.platform.seller.internal.command.model.SellerSalesChannelCommands.RevokeSellerPublishableKeyCommand;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.ProvisionOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.PublishableKeyOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerSalesChannelCommandService {

    private final CommandBus commandBus;
    private final ProvisionSellerSalesChannelMapper provisionMapper;
    private final CreateSellerPublishableKeyMapper createKeyMapper;
    private final RevokeSellerPublishableKeyMapper revokeKeyMapper;

    public SellerSalesChannelCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            ProvisionSellerSalesChannelMapper provisionMapper,
            CreateSellerPublishableKeyMapper createKeyMapper,
            RevokeSellerPublishableKeyMapper revokeKeyMapper
    ) {
        this.commandBus = commandBus;
        this.provisionMapper = provisionMapper;
        this.createKeyMapper = createKeyMapper;
        this.revokeKeyMapper = revokeKeyMapper;
    }

    public SalesChannelProvisionResponse provisionMerchantWebsiteChannel(String merchantIdStr, ProvisionWebsiteChannelRequest request) {
        ProvisionSellerMerchantWebsiteChannelCommand cmd = provisionMapper.toCommand(merchantIdStr, request);
        ProvisionOutboundResult result = commandBus.dispatch(cmd);
        return provisionMapper.toResponse(result);
    }

    public PublishableKeyResponse createPublishableKey(String channelIdStr, CreatePublishableKeyRequest request) {
        CreateSellerPublishableKeyCommand cmd = createKeyMapper.toCommand(channelIdStr, request);
        PublishableKeyOutboundResult result = commandBus.dispatch(cmd);
        return createKeyMapper.toResponse(result);
    }

    public void revokePublishableKey(String channelIdStr, String keyIdStr) {
        RevokeSellerPublishableKeyCommand cmd = revokeKeyMapper.toCommand(channelIdStr, keyIdStr);
        commandBus.dispatch(cmd);
    }
}
