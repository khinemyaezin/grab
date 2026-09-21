package com.region.application.port.inbound;

import com.region.application.model.read.FindRegionSliceQuery;
import com.region.application.model.read.FindRegionSliceResult;

import java.util.Optional;

public interface FindRegionSliceUseCase {
    Optional<FindRegionSliceResult> execute(FindRegionSliceQuery query);
}
