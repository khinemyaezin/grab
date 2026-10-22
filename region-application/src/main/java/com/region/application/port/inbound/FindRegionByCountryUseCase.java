package com.region.application.port.inbound;

import com.region.application.model.read.FindRegionByCountryQuery;
import com.region.application.model.read.RegionResult;

import java.util.Optional;

public interface FindRegionByCountryUseCase {
    Optional<RegionResult> execute(FindRegionByCountryQuery query);
}
