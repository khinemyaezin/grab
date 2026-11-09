package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateProductVariantWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartSellerUpdateProductVariantWorkflowCommandHandler
        implements CommandHandler<StartUpdateProductVariantWorkflowCommand, UpdateProductVariantResponse> {

    private final SellerWorkflowOutboundPort outboundPort;

    public StartSellerUpdateProductVariantWorkflowCommandHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public UpdateProductVariantResponse handle(StartUpdateProductVariantWorkflowCommand command) {
        return outboundPort.startUpdateProductVariant(command.message());
    }

    @Override
    public Class<StartUpdateProductVariantWorkflowCommand> getCommandType() {
        return StartUpdateProductVariantWorkflowCommand.class;
    }
}

