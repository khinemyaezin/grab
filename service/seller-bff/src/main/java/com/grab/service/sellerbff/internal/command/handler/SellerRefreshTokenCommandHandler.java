package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerRefreshTokenCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerAuthTokensResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SellerRefreshTokenCommandHandler implements CommandHandler<SellerRefreshTokenCommand, SellerAuthTokensResult> {

    private final SellerAuthOutboundPort outboundPort;

    public SellerRefreshTokenCommandHandler(SellerAuthOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public SellerAuthTokensResult handle(SellerRefreshTokenCommand command) {
        return outboundPort.refreshToken(command.refreshToken());
    }

    @Override
    public Class<SellerRefreshTokenCommand> getCommandType() {
        return SellerRefreshTokenCommand.class;
    }
}

