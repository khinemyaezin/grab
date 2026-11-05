package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartCreateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.CreateSellableProductRecord;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartCreateSellableProductWorkflowCommandHandler
        implements CommandHandler<StartCreateSellableProductWorkflowCommand, WorkflowExecutionOutboundResult> {

    private final SellerWorkflowOutboundPort outboundPort;

    public StartCreateSellableProductWorkflowCommandHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public WorkflowExecutionOutboundResult handle(StartCreateSellableProductWorkflowCommand command) {
        CreateSellableProductRecord record = new CreateSellableProductRecord(
                command.merchantId(),
                command.title(),
                command.sku(),
                command.price(),
                command.currencyCode(),
                command.initialStock(),
                command.locationId(),
                command.salesChannelIds(),
                command.attributes()
        );
        return outboundPort.startCreateSellableProduct(record);
    }

    @Override
    public Class<StartCreateSellableProductWorkflowCommand> getCommandType() {
        return StartCreateSellableProductWorkflowCommand.class;
    }
}

