package com.grab.store.pricing.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.pricing.internal.command.PricePreferenceResult;

import java.util.List;

public record ListPricePreferencesQuery() implements Query<List<PricePreferenceResult>> {
}
