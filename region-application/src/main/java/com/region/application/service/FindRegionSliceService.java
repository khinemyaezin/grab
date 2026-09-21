package com.region.application.service;

import com.region.application.model.read.FindRegionSliceQuery;
import com.region.application.model.read.FindRegionSliceResult;
import com.region.application.port.inbound.FindRegionSliceUseCase;
import com.region.application.port.outbound.RegionQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindRegionSliceService implements FindRegionSliceUseCase {

    private final RegionQueryPort regionQueryPort;

    @Override
    public Optional<FindRegionSliceResult> execute(FindRegionSliceQuery query) {
        return regionQueryPort.findById(query.regionId())
                .map(view -> new FindRegionSliceResult(view.id(), view.currencyCode(), view.status()));
    }
}
