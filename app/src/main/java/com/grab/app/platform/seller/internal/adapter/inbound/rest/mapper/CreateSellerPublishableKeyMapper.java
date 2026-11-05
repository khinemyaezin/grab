package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PublishableKeyResponse;
import com.grab.app.platform.seller.internal.command.model.SellerSalesChannelCommands.CreateSellerPublishableKeyCommand;
import com.grab.app.platform.seller.port.outbound.SellerSalesChannelOutboundPort.PublishableKeyOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateSellerPublishableKeyMapper {

    @Mapping(target = "salesChannelId", source = "salesChannelId")
    @Mapping(target = "label", source = "request.label")
    public abstract CreateSellerPublishableKeyCommand toCommand(String salesChannelId, CreatePublishableKeyRequest request);

    public abstract PublishableKeyResponse toResponse(PublishableKeyOutboundResult result);
}

