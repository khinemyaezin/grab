package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.application.model.write.CreateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.port.inbound.CreateLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateLocationCommandHandler implements CommandHandler<CreateLocationCommand, LocationResult> {

    private final CreateLocationUseCase createLocationUseCase;

    @Override
    @InventoryTransactional
    public LocationResult handle(CreateLocationCommand command) {
        return createLocationUseCase.execute(command);
    }

    @Override
    public Class<CreateLocationCommand> getCommandType() {
        return CreateLocationCommand.class;
    }
}
