package com.inventory.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.LocationType;

public record CreateLocationCommand(
        String merchantId,
        String code,
        String name,
        LocationType type,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,
        Id createdBy
) implements Command<LocationResult> {
}
