package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.CreateBinCommand;
import com.inventory.application.port.inbound.CreateBinUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateBinCommandHandler implements CommandHandler<CreateBinCommand, BinResult> {

    private final CreateBinUseCase createBinUseCase;

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
