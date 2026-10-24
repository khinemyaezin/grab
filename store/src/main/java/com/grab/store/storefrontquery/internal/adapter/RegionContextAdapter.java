package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.framework.id.Id;
import com.grab.store.region.port.RegionQuery;
import com.grab.store.storefrontquery.internal.adapter.mapper.RegionContextMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegionContextAdapter implements RegionContextPort {
    private final RegionQuery regions;
    private final RegionContextMapper mapper;

    @Override
    public Optional<RegionView> findByCountry(String countryCode) {
        return regions.findByCountry(countryCode).map(this::mapRegion);
    }

    @Override
    public Optional<RegionView> findById(Id regionId) {
        String regionValue = regionId.getValue();
        return regions.find(regionValue)
                .map(this::mapRegion);
    }

    private RegionView mapRegion(RegionQuery.RegionSlice regionSlice) {
        RegionView view = mapper.toRegionView(regionSlice);
        if (view == null || view.regionId() == null) {
            throw new IllegalStateException("Region provider returned invalid metadata");
        }
        return view;
    }
}
