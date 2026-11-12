package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.GrantAuthorityCommand;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class GrantAuthorityCommandHandler implements CommandHandler<GrantAuthorityCommand, Void> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public GrantAuthorityCommandHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Void handle(GrantAuthorityCommand command) {
        outboundPort.grantAuthorityToRole(command.roleCode(), command.authorityCode());
        return null;
    }

    @Override
    public Class<GrantAuthorityCommand> getCommandType() {
        return GrantAuthorityCommand.class;
    }
}

