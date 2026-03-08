package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.CreateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateLocationCommandHandler implements CommandHandler<CreateLocationCommand, LocationResult> {

    private final LocationRepository locationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public LocationResult handle(CreateLocationCommand command) {
        if (locationRepository.existsByCode(command.code())) {
            throw new IllegalArgumentException("Location already exists for code: " + command.code());
        }

        Location location = new Location(
                idGenerator.generateId(),
                command.code(),
                command.name(),
                command.type(),
                new Address(
                        command.line1(),
                        command.line2(),
                        command.city(),
                        command.state(),
                        command.postalCode(),
                        command.country()
                )
        );

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
    }

    @Override
    public Class<CreateLocationCommand> getCommandType() {
        return CreateLocationCommand.class;
    }
}
