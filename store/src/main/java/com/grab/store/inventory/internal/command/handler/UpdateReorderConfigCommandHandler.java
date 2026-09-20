package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.UpdateReorderConfigCommand;
import com.inventory.application.port.inbound.UpdateReorderConfigUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateReorderConfigCommandHandler implements CommandHandler<UpdateReorderConfigCommand, InventoryItemResult> {

    private final UpdateReorderConfigUseCase updateReorderConfigUseCase;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(UpdateReorderConfigCommand command) {
        return updateReorderConfigUseCase.execute(command);
    }

    @Override
    public Class<UpdateReorderConfigCommand> getCommandType() {
        return UpdateReorderConfigCommand.class;
    }
}
