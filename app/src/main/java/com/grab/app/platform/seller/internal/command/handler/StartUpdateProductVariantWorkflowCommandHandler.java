package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.UpdateProductVariantRecord;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort.WorkflowExecutionOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartUpdateProductVariantWorkflowCommandHandler
        implements CommandHandler<StartUpdateProductVariantWorkflowCommand, WorkflowExecutionOutboundResult> {

    private final SellerWorkflowOutboundPort outboundPort;

    public StartUpdateProductVariantWorkflowCommandHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public WorkflowExecutionOutboundResult handle(StartUpdateProductVariantWorkflowCommand command) {
        UpdateProductVariantRecord record = new UpdateProductVariantRecord(
                command.merchantId(),
                command.title(),
                command.price(),
                command.currencyCode(),
                command.stockDelta(),
                command.optionValues()
        );
        return outboundPort.startUpdateProductVariant(command.productId(), command.variantId(), record);
    }

    @Override
    public Class<StartUpdateProductVariantWorkflowCommand> getCommandType() {
        return StartUpdateProductVariantWorkflowCommand.class;
    }
}

