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
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateLocationCommandHandler implements CommandHandler<UpdateLocationCommand, LocationResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryTransactional
    public LocationResult handle(UpdateLocationCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue())));

        if (command.code() != null && !command.code().isBlank() && !command.code().equals(location.getCode())) {
            if (locationRepository.existsByCode(command.code())) {
                throw new InventoryServiceException(new InventoryServiceError.LocationAlreadyExists(command.code()));
            }
            location.setCode(command.code());
        }

        if (command.name() != null && !command.name().isBlank()) {
            location.setName(command.name());
        }

        if (command.type() != null) {
            location.setType(command.type());
        }

        if (command.addressProvided()) {
            location.setAddress(mergeAddress(location.getAddress(), command));
        }

        Location saved = locationRepository.save(location);
        return LocationResultMapper.toCommandResult(saved);
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
