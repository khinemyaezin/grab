package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.CreateSellableProductMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateProductVariantMessage;
import com.grab.app.workflow.port.message.WorkflowExecutionPort.UpdateSellableProductMessage;
import com.grab.framework.cqrs.command.Command;

public final class SellerWorkflowCommands {

    private SellerWorkflowCommands() {}

    public record StartCreateSellableProductWorkflowCommand(
            CreateSellableProductMessage message
    ) implements Command<CreateSellableProductResponse> {}

    public record StartUpdateSellableProductWorkflowCommand(
            UpdateSellableProductMessage message
    ) implements Command<UpdateSellableProductResponse> {}

    public record StartUpdateProductVariantWorkflowCommand(
            UpdateProductVariantMessage message
    ) implements Command<UpdateProductVariantResponse> {}
}
