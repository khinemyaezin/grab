package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetZoneQuery(
        Id zoneId
) implements Query<GetZoneResult> {
}
