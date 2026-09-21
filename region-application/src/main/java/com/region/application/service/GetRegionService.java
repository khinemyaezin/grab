package com.region.application.service;

import com.region.application.model.read.GetRegionQuery;
import com.region.application.model.read.RegionResult;
import com.region.application.port.inbound.GetRegionUseCase;
import com.region.application.port.outbound.RegionQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class GetRegionService implements GetRegionUseCase {

    private final RegionQueryPort regionQueryPort;

    @Override
    public Optional<RegionResult> execute(GetRegionQuery query) {
        return regionQueryPort.findById(query.regionId()).map(view -> new RegionResult(
                view.id(),
                view.name(),
                view.currencyCode(),
                view.status(),
                view.countryCodes()
        ));
    }
}
