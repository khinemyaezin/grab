package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.UpdateReorderConfigCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.port.inbound.UpdateReorderConfigUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdateReorderConfigCommandHandler implements CommandHandler<UpdateReorderConfigCommand, InventoryItemResult> {

    private final UpdateReorderConfigUseCase updateReorderConfigUseCase;

    public UpdateReorderConfigCommandHandler(UpdateReorderConfigUseCase updateReorderConfigUseCase) {
        this.updateReorderConfigUseCase = updateReorderConfigUseCase;
    }

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
