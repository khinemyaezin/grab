package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetBinQuery(
        Id binId
) implements Query<GetBinResult> {
}
