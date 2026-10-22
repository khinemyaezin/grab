package com.region.application.model.read;

import com.grab.framework.cqrs.query.Query;

import java.util.Optional;

public record FindRegionByCountryQuery(String countryCode) implements Query<Optional<RegionResult>> {
}
