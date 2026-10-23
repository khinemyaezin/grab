package com.grab.store.region.internal.api.adapter.mapper;

import com.grab.store.region.port.RegionQuery.RegionSlice;
import com.region.application.model.read.FindRegionSliceResult;
import com.region.application.model.read.RegionResult;
import org.springframework.stereotype.Component;
import org.mapstruct.Mapper;

import com.grab.store.region.internal.api.rest.mapper.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public abstract class RegionQueryMapper {

    public abstract RegionSlice toSlice(FindRegionSliceResult slice);

    public abstract RegionSlice toSlice(RegionResult region);
}
