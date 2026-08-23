package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.CreateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateLocationCommandHandler implements CommandHandler<CreateLocationCommand, LocationResult> {

    private static final Logger log = Loggers.getLogger(CreateLocationCommandHandler.class);

    private final LocationRepository locationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public LocationResult handle(CreateLocationCommand command) {
        log.info("Creating location with code={}", command.code());
        
        if (locationRepository.existsByCode(command.code())) {
            log.warn("Location already exists with code={}", command.code());
            throw new InventoryServiceException(new InventoryServiceError.LocationAlreadyExists(command.code()));
        }

        Location location = Location.create(
                idGenerator.generateId(),
                idGenerator.convertIdFrom(command.merchantId()),
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

        log.info("Created location with id={}, code={}", saved.getId().getValue(), saved.getCode());

        return new LocationResult(
                saved.getId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getType().name(),
                saved.isActive(),
                new LocationResult.Address(
                        saved.getAddress().line1(),
                        saved.getAddress().line2(),
                        saved.getAddress().city(),
                        saved.getAddress().state(),
                        saved.getAddress().postalCode(),
                        saved.getAddress().country()
                )
        );
    }

    @Override
    public Class<CreateLocationCommand> getCommandType() {
        return CreateLocationCommand.class;
    }
}
