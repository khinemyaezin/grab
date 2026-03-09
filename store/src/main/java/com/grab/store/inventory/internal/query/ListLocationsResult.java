package com.grab.store.inventory.internal.query;

import java.util.List;

public record ListLocationsResult(
        List<GetLocationResult> items
) {
}
