package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartUpdateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartUpdateSellableProductWorkflowCommandHandler
        implements CommandHandler<StartUpdateSellableProductWorkflowCommand, UpdateSellableProductResponse> {

    private final SellerWorkflowOutboundPort outboundPort;

    public StartUpdateSellableProductWorkflowCommandHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public UpdateSellableProductResponse handle(StartUpdateSellableProductWorkflowCommand command) {
        return outboundPort.startUpdateSellableProduct(command.message());
    }

    @Override
    public Class<StartUpdateSellableProductWorkflowCommand> getCommandType() {
        return StartUpdateSellableProductWorkflowCommand.class;
    }
}
