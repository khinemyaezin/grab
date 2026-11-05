package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartCreateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartCreateSellableProductWorkflowMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "sku", source = "request.sku")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "currencyCode", source = "request.currencyCode")
    @Mapping(target = "initialStock", source = "request.initialStock")
    @Mapping(target = "locationId", source = "request.locationId")
    @Mapping(target = "salesChannelIds", source = "request.salesChannelIds")
    @Mapping(target = "attributes", source = "request.attributes")
    public abstract StartCreateSellableProductWorkflowCommand toCommand(String merchantId, CreateSellableProductWorkflowRequest request);

    public abstract WorkflowExecutionResponse toResponse(WorkflowExecutionOutboundResult result);
}

