package com.grab.store.region.internal.api.adapter;

import com.grab.store.region.internal.api.adapter.mapper.RegionQueryMapper;
import com.grab.store.region.internal.config.RegionReadTransactional;
import com.grab.store.region.port.RegionQuery;
import com.region.application.model.read.FindRegionSliceQuery;
import com.region.application.port.inbound.FindRegionSliceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegionQueryAdapter implements RegionQuery {

    private final FindRegionSliceUseCase findRegionSliceUseCase;
    private final RegionQueryMapper mapper;

    @Override
    @RegionReadTransactional
    public Optional<RegionSlice> find(String regionId) {
        return findRegionSliceUseCase.execute(new FindRegionSliceQuery(regionId))
                .map(mapper::toSlice);
    }
}
