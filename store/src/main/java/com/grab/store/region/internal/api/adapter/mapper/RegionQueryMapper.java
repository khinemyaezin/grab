package com.grab.store.region.internal.api.adapter.mapper;

import com.grab.store.region.port.RegionQuery.RegionSlice;
import com.region.application.model.read.FindRegionSliceResult;
import org.springframework.stereotype.Component;

@Component
public class RegionQueryMapper {

    public RegionSlice toSlice(FindRegionSliceResult slice) {
        return new RegionSlice(slice.regionId(), slice.currencyCode(), slice.status());
    }
}
