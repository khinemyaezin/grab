package com.grab.app.merchandising.inventory.internal.command.handler;

import com.grab.app.merchandising.inventory.internal.config.InventoryTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantCommand;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantResult;
import com.inventory.application.port.inbound.DiscontinueInventoryForDeletedVariantUseCase;
import org.springframework.stereotype.Component;

@Component
public class DiscontinueInventoryForDeletedVariantCommandHandler implements CommandHandler<DiscontinueInventoryForDeletedVariantCommand, DiscontinueInventoryForDeletedVariantResult> {

    private final DiscontinueInventoryForDeletedVariantUseCase discontinueInventoryForDeletedVariantUseCase;

    public DiscontinueInventoryForDeletedVariantCommandHandler(DiscontinueInventoryForDeletedVariantUseCase discontinueInventoryForDeletedVariantUseCase) {
        this.discontinueInventoryForDeletedVariantUseCase = discontinueInventoryForDeletedVariantUseCase;
    }

    @Override
    @InventoryTransactional
    public DiscontinueInventoryForDeletedVariantResult handle(DiscontinueInventoryForDeletedVariantCommand command) {
        return discontinueInventoryForDeletedVariantUseCase.execute(command);
    }

    @Override
    public Class<DiscontinueInventoryForDeletedVariantCommand> getCommandType() {
        return DiscontinueInventoryForDeletedVariantCommand.class;
    }
}
