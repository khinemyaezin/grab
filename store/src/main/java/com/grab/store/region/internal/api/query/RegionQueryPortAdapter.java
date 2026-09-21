package com.grab.store.region.internal.api.query;

import com.grab.store.region.internal.config.RegionReadTransactional;
import com.grab.store.region.query.RegionQueryPort;
import com.region.application.model.read.FindRegionSliceQuery;
import com.region.application.port.inbound.FindRegionSliceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegionQueryPortAdapter implements RegionQueryPort {
    private final FindRegionSliceUseCase findRegionSliceUseCase;

    @Override
    @RegionReadTransactional
    public Optional<RegionSlice> find(String regionId) {
        return findRegionSliceUseCase.execute(new FindRegionSliceQuery(regionId))
                .map(slice -> new RegionSlice(slice.regionId(), slice.currencyCode(), slice.status()));
    }
}
