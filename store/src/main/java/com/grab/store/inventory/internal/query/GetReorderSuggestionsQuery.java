package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;

public record GetReorderSuggestionsQuery(
        Id merchantId,
        Id locationId,
        String sku
) implements Query<List<GetReorderSuggestionResult>> {
}
