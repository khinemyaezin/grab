package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerLoginCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SellerLoginCommandHandler implements CommandHandler<SellerLoginCommand, SellerAuthTokensResult> {

    private final SellerAuthOutboundPort outboundPort;

    public SellerLoginCommandHandler(SellerAuthOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public SellerAuthTokensResult handle(SellerLoginCommand command) {
        return outboundPort.login(command.email(), command.password());
    }

    @Override
    public Class<SellerLoginCommand> getCommandType() {
        return SellerLoginCommand.class;
    }
}

