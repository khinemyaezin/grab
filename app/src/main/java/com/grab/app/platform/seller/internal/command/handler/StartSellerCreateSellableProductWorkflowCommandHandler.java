package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.command.model.SellerWorkflowCommands.StartCreateSellableProductWorkflowCommand;
import com.grab.app.platform.seller.port.outbound.SellerWorkflowOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class StartSellerCreateSellableProductWorkflowCommandHandler
        implements CommandHandler<StartCreateSellableProductWorkflowCommand, CreateSellableProductResponse> {

    private final SellerWorkflowOutboundPort outboundPort;

    public StartSellerCreateSellableProductWorkflowCommandHandler(SellerWorkflowOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public CreateSellableProductResponse handle(StartCreateSellableProductWorkflowCommand command) {
        return outboundPort.startCreateSellableProduct(command.message());
    }

    @Override
    public Class<StartCreateSellableProductWorkflowCommand> getCommandType() {
        return StartCreateSellableProductWorkflowCommand.class;
    }
}

