package com.pricing.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.pricing.application.model.write.PricePreferenceResult;

import java.util.List;

public record ListPricePreferencesQuery() implements Query<List<PricePreferenceResult>> {
}
