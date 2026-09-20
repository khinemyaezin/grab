package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantCommand;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantResult;
import com.inventory.application.port.inbound.DiscontinueInventoryForDeletedVariantUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscontinueInventoryForDeletedVariantCommandHandler implements CommandHandler<DiscontinueInventoryForDeletedVariantCommand, DiscontinueInventoryForDeletedVariantResult> {

    private final DiscontinueInventoryForDeletedVariantUseCase discontinueInventoryForDeletedVariantUseCase;

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
