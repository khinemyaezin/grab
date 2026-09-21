package com.region.application.port.inbound;

import com.region.application.model.read.GetRegionQuery;
import com.region.application.model.read.RegionResult;

import java.util.Optional;

public interface GetRegionUseCase {
    Optional<RegionResult> execute(GetRegionQuery query);
}
