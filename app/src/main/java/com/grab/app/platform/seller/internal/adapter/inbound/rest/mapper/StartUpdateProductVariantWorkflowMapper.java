package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartUpdateProductVariantWorkflowMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "variantId", source = "variantId")
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "currencyCode", source = "request.currencyCode")
    @Mapping(target = "stockDelta", source = "request.stockDelta")
    @Mapping(target = "optionValues", source = "request.optionValues")
    public abstract StartUpdateProductVariantWorkflowCommand toCommand(String productId, String variantId, String merchantId, UpdateProductVariantWorkflowRequest request);

    public abstract WorkflowExecutionResponse toResponse(WorkflowExecutionOutboundResult result);
}

