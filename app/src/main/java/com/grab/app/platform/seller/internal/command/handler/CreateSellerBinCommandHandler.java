package com.grab.app.platform.seller.internal.command.handler;

import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerBinCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.CreateBinRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
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
