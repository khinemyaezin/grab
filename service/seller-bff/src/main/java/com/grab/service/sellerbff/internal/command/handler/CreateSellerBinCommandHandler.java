package com.grab.service.sellerbff.internal.command.handler;

import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.CreateSellerBinCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.CreateBinRecord;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellerBinCommandHandler
        implements CommandHandler<CreateSellerBinCommand, FacilityBinOutboundResult> {

    private final SellerInventoryOutboundPort outboundPort;

    public CreateSellerBinCommandHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public FacilityBinOutboundResult handle(CreateSellerBinCommand command) {
        CreateBinRecord record = new CreateBinRecord(
                command.zoneId(),
                command.actorId(),
                command.code(),
                command.name(),
                command.maxCapacity()
        );
        return outboundPort.createBin(record);
    }

    @Override
    public Class<CreateSellerBinCommand> getCommandType() {
        return CreateSellerBinCommand.class;
    }
}
