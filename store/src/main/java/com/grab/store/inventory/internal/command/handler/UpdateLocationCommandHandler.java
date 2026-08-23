package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.valueobject.Address;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.UpdateLocationCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateLocationCommandHandler implements CommandHandler<UpdateLocationCommand, LocationResult> {

    private static final Logger log = Loggers.getLogger(UpdateLocationCommandHandler.class);

    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

    @Override
    @InventoryTransactional
    public LocationResult handle(UpdateLocationCommand command) {
        log.info("Updating location with id={}", command.locationId().getValue());
        
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);

        if (command.code() != null && !command.code().isBlank() && !command.code().equals(location.getCode())) {
            if (locationRepository.existsByCode(command.code())) {
                log.warn("Location code already exists: code={}", command.code());
                throw new InventoryServiceException(new InventoryServiceError.LocationAlreadyExists(command.code()));
            }
        }

        Address mergedAddress = null;
        if (command.addressProvided()) {
            mergedAddress = mergeAddress(location.getAddress(), command);
        }

        location.update(command.code(), command.name(), command.type(), mergedAddress);

        Location saved = locationRepository.save(location);

        log.info("Updated location with id={}, code={}", saved.getId().getValue(), saved.getCode());

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
    public Class<UpdateLocationCommand> getCommandType() {
        return UpdateLocationCommand.class;
    }

    private Address mergeAddress(Address existing, UpdateLocationCommand command) {
        String line1 = command.line1() != null ? command.line1() : existingValue(existing, 1);
        String line2 = command.line2() != null ? command.line2() : existingValue(existing, 2);
        String city = command.city() != null ? command.city() : existingValue(existing, 3);
        String state = command.state() != null ? command.state() : existingValue(existing, 4);
        String postalCode = command.postalCode() != null ? command.postalCode() : existingValue(existing, 5);
        String country = command.country() != null ? command.country() : existingValue(existing, 6);

        if (country == null || country.isBlank()) {
            throw new InventoryServiceException(new InventoryServiceError.AddressCountryRequired());
        }

        return new Address(line1, line2, city, state, postalCode, country);
    }

    private String existingValue(Address address, int field) {
        if (address == null) {
            return null;
        }
        return switch (field) {
            case 1 -> address.line1();
            case 2 -> address.line2();
            case 3 -> address.city();
            case 4 -> address.state();
            case 5 -> address.postalCode();
            case 6 -> address.country();
            default -> null;
        };
    }
}
