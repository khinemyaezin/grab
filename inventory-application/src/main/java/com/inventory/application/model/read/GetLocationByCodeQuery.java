package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetLocationByCodeQuery(
        String code
) implements Query<GetLocationResult> {
}
