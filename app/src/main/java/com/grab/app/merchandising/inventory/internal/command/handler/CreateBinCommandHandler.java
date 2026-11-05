package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.CreateBinCommand;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.port.inbound.CreateBinUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreateBinCommandHandler implements CommandHandler<CreateBinCommand, BinResult> {

    private final CreateBinUseCase createBinUseCase;

    public CreateBinCommandHandler(CreateBinUseCase createBinUseCase) {
        this.createBinUseCase = createBinUseCase;
    }

    @Override
    @InventoryTransactional
    public BinResult handle(CreateBinCommand command) {
        return createBinUseCase.execute(command);
    }

    @Override
    public Class<CreateBinCommand> getCommandType() {
        return CreateBinCommand.class;
    }
}
