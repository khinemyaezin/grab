package com.inventory.application.service;

import com.inventory.application.port.inbound.CreateLocationUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.inventory.application.model.write.CreateLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateLocationService implements CreateLocationUseCase {

    private static final Logger log = Loggers.getLogger(CreateLocationService.class);

    private final LocationRepository locationRepository;
    private final IdGenerator idGenerator;

            public LocationResult execute(CreateLocationCommand command) {
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

        public Class<CreateLocationCommand> getCommandType() {
        return CreateLocationCommand.class;
    }
}
