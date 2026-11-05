package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartCreateSellableProductWorkflowMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartUpdateProductVariantWorkflowMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.StartUpdateSellableProductWorkflowMapper;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartCreateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerWorkflowCommandService {

    private final CommandBus commandBus;
    private final StartCreateSellableProductWorkflowMapper startCreateWorkflowMapper;
    private final StartUpdateSellableProductWorkflowMapper startUpdateWorkflowMapper;
    private final StartUpdateProductVariantWorkflowMapper startUpdateVariantWorkflowMapper;

    public SellerWorkflowCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            StartCreateSellableProductWorkflowMapper startCreateWorkflowMapper,
            StartUpdateSellableProductWorkflowMapper startUpdateWorkflowMapper,
            StartUpdateProductVariantWorkflowMapper startUpdateVariantWorkflowMapper
    ) {
        this.commandBus = commandBus;
        this.startCreateWorkflowMapper = startCreateWorkflowMapper;
        this.startUpdateWorkflowMapper = startUpdateWorkflowMapper;
        this.startUpdateVariantWorkflowMapper = startUpdateVariantWorkflowMapper;
    }

    public WorkflowExecutionResponse startCreateSellableProduct(String merchantIdStr, CreateSellableProductWorkflowRequest request) {
        StartCreateSellableProductWorkflowCommand cmd = startCreateWorkflowMapper.toCommand(merchantIdStr, request);
        WorkflowExecutionOutboundResult result = commandBus.dispatch(cmd);
        return startCreateWorkflowMapper.toResponse(result);
    }

    public WorkflowExecutionResponse startUpdateSellableProduct(String productIdStr, String merchantIdStr, UpdateSellableProductWorkflowRequest request) {
        StartUpdateSellableProductWorkflowCommand cmd = startUpdateWorkflowMapper.toCommand(productIdStr, merchantIdStr, request);
        WorkflowExecutionOutboundResult result = commandBus.dispatch(cmd);
        return startUpdateWorkflowMapper.toResponse(result);
    }

    public WorkflowExecutionResponse startUpdateProductVariant(String productIdStr, String variantIdStr, String merchantIdStr, UpdateProductVariantWorkflowRequest request) {
        StartUpdateProductVariantWorkflowCommand cmd = startUpdateVariantWorkflowMapper.toCommand(productIdStr, variantIdStr, merchantIdStr, request);
        WorkflowExecutionOutboundResult result = commandBus.dispatch(cmd);
        return startUpdateVariantWorkflowMapper.toResponse(result);
    }
}
