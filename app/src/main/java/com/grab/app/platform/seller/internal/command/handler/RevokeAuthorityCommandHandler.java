package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.RevokeAuthorityCommand;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class RevokeAuthorityCommandHandler implements CommandHandler<RevokeAuthorityCommand, Void> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public RevokeAuthorityCommandHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(RevokeAuthorityCommand command) {
        outboundPort.revokeAuthorityFromRole(command.roleCode(), command.authorityCode());
        return null;
    }

    @Override
    public Class<RevokeAuthorityCommand> getCommandType() {
        return RevokeAuthorityCommand.class;
    }
}

