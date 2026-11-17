package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerAuthCommands.SellerRegisterUserCommand;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SellerRegisterUserCommandHandler implements CommandHandler<SellerRegisterUserCommand, SellerUserRegistrationResult> {

    private final SellerAuthOutboundPort outboundPort;

    public SellerRegisterUserCommandHandler(SellerAuthOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public SellerUserRegistrationResult handle(SellerRegisterUserCommand command) {
        return outboundPort.registerUser(command.email(), command.password());
    }

    @Override
    public Class<SellerRegisterUserCommand> getCommandType() {
        return SellerRegisterUserCommand.class;
    }
}

