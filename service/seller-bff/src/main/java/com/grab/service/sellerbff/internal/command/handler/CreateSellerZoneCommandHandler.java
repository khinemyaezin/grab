package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.CreateSellerZoneCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.CreateZoneRecord;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerZoneCommandHandler
        implements CommandHandler<CreateSellerZoneCommand, FacilityZoneOutboundResult> {

    private final SellerInventoryOutboundPort outboundPort;

    public CreateSellerZoneCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public FacilityZoneOutboundResult handle(CreateSellerZoneCommand command) {
        CreateZoneRecord record = new CreateZoneRecord(
                command.locationId(),
                command.actorId(),
                command.code(),
                command.name(),
                command.type()
        );
        return outboundPort.createZone(record);
    }

    @Override
    public Class<CreateSellerZoneCommand> getCommandType() {
        return CreateSellerZoneCommand.class;
    }
}

