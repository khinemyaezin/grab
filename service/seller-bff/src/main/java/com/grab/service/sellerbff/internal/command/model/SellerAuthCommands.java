package com.grab.service.sellerbff.internal.command.model;

import com.grab.service.sellerbff.port.outbound.SellerAuthOutboundPort.SellerUserRegistrationResult;
import com.grab.framework.cqrs.command.Command;

public final class SellerAuthCommands {

    private SellerAuthCommands() {}

    public record SellerRegisterUserCommand(
            String email,
            String password
    ) implements Command<SellerUserRegistrationResult> {}
}

