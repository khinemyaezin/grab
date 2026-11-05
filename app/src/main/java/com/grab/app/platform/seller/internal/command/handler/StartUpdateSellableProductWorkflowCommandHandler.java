package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.UpdateSellableProductRecord;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartUpdateSellableProductWorkflowCommandHandler
        implements CommandHandler<StartUpdateSellableProductWorkflowCommand, WorkflowExecutionOutboundResult> {

    private final SellerWorkflowOutboundPort outboundPort;

    public StartUpdateSellableProductWorkflowCommandHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public WorkflowExecutionOutboundResult handle(StartUpdateSellableProductWorkflowCommand command) {
        UpdateSellableProductRecord record = new UpdateSellableProductRecord(
                command.merchantId(),
                command.title(),
                command.description(),
                command.categoryId(),
                command.publishSalesChannelIds(),
                command.unpublishSalesChannelIds(),
                command.attributes()
        );
        return outboundPort.startUpdateSellableProduct(command.productId(), record);
    }

    @Override
    public Class<StartUpdateSellableProductWorkflowCommand> getCommandType() {
        return StartUpdateSellableProductWorkflowCommand.class;
    }
}

