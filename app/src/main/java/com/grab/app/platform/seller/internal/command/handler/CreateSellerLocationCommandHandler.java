package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerLocationCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.CreateLocationRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerLocationCommandHandler
        implements CommandHandler<CreateSellerLocationCommand, FacilityLocationOutboundResult> {

    private final SellerInventoryOutboundPort outboundPort;

    public CreateSellerLocationCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public FacilityLocationOutboundResult handle(CreateSellerLocationCommand command) {
        CreateLocationRecord record = new CreateLocationRecord(
                command.merchantId(),
                command.actorId(),
                command.code(),
                command.name(),
                command.type(),
                command.line1(),
                command.line2(),
                command.city(),
                command.state(),
                command.postalCode(),
                command.country()
        );
        return outboundPort.createLocation(record);
    }

    @Override
    public Class<CreateSellerLocationCommand> getCommandType() {
        return CreateSellerLocationCommand.class;
    }
}

