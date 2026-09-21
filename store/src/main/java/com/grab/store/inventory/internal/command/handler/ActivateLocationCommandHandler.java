package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.ActivateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.port.inbound.ActivateLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateLocationCommandHandler implements CommandHandler<ActivateLocationCommand, LocationResult> {

    private final ActivateLocationUseCase activateLocationUseCase;

    @Override
    @InventoryTransactional
    public LocationResult handle(ActivateLocationCommand command) {
        return activateLocationUseCase.execute(command);
    }

    @Override
    public Class<ActivateLocationCommand> getCommandType() {
        return ActivateLocationCommand.class;
    }
}
