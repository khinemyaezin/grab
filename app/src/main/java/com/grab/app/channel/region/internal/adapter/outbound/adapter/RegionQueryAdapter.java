package com.grab.app.channel.region.internal.adapter.outbound;

import com.grab.app.channel.region.internal.adapter.outbound.mapper.RegionQueryMapper;
import com.grab.app.channel.region.internal.config.RegionReadTransactional;
import com.grab.app.channel.region.port.RegionQuery;
import com.region.application.model.read.FindRegionSliceQuery;
import com.region.application.model.read.FindRegionByCountryQuery;
import com.region.application.port.inbound.FindRegionSliceUseCase;
import com.region.application.port.inbound.FindRegionByCountryUseCase;
import com.region.application.port.inbound.ListActiveCountryCodesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RegionQueryAdapter implements RegionQuery {

    private final FindRegionSliceUseCase findRegionSliceUseCase;
    private final FindRegionByCountryUseCase findRegionByCountryUseCase;
    private final ListActiveCountryCodesUseCase findActiveCountryCodesUseCase;
    private final RegionQueryMapper mapper;

    @Override
    @RegionReadTransactional
    public Optional<RegionSlice> find(String regionId) {
        return findRegionSliceUseCase.execute(new FindRegionSliceQuery(regionId))
                .map(mapper::toSlice);
    }

    @Override
    @RegionReadTransactional
    public Optional<RegionSlice> findByCountry(String countryCode) {
        return findRegionByCountryUseCase.execute(new FindRegionByCountryQuery(countryCode))
                .map(mapper::toSlice);
    }

    @Override
    @RegionReadTransactional
    public List<String> findActiveCountryCodes() {
        return findActiveCountryCodesUseCase.execute(new com.region.application.model.read.ListActiveCountryCodesQuery());
    }
}
