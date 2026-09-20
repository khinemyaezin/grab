package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetLocationQuery(
        Id locationId
) implements Query<GetLocationResult> {
}
