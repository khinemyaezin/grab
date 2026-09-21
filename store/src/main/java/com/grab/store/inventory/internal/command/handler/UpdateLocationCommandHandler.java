package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.model.write.UpdateLocationCommand;
import com.inventory.application.port.inbound.UpdateLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateLocationCommandHandler implements CommandHandler<UpdateLocationCommand, LocationResult> {

    private final UpdateLocationUseCase updateLocationUseCase;

    @Override
    @InventoryTransactional
    public LocationResult handle(UpdateLocationCommand command) {
        return updateLocationUseCase.execute(command);
    }

    @Override
    public Class<UpdateLocationCommand> getCommandType() {
        return UpdateLocationCommand.class;
    }
}
