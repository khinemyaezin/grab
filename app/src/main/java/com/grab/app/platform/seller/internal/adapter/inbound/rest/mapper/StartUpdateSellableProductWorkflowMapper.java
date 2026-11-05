package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartUpdateSellableProductWorkflowMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "categoryId", source = "request.categoryId")
    @Mapping(target = "publishSalesChannelIds", source = "request.publishSalesChannelIds")
    @Mapping(target = "unpublishSalesChannelIds", source = "request.unpublishSalesChannelIds")
    @Mapping(target = "attributes", source = "request.attributes")
    public abstract StartUpdateSellableProductWorkflowCommand toCommand(String productId, String merchantId, UpdateSellableProductWorkflowRequest request);

    public abstract WorkflowExecutionResponse toResponse(WorkflowExecutionOutboundResult result);
}

