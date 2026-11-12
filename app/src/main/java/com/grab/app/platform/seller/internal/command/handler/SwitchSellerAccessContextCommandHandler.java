package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.command.model.SellerIdentityAdminCommands.SwitchAccessContextCommand;
import com.grab.app.platform.seller.port.outbound.SellerIdentityAdminOutboundPort;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SwitchSellerAccessContextCommandHandler implements CommandHandler<SwitchAccessContextCommand, SellerAuthResponse> {

    private final SellerIdentityAdminOutboundPort outboundPort;

    public SwitchSellerAccessContextCommandHandler(SellerIdentityAdminOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public SellerAuthResponse handle(SwitchAccessContextCommand command) {
        return outboundPort.switchAccessContext(
                command.userId(),
                command.targetAssignmentId(),
                command.currentRefreshToken()
        );
    }

    @Override
    public Class<SwitchAccessContextCommand> getCommandType() {
        return SwitchAccessContextCommand.class;
    }
}

