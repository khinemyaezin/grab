package com.grab.app.channel.region.internal.adapter.outbound.mapper;

import com.grab.app.channel.region.port.RegionQuery.RegionSlice;
import com.region.application.model.read.FindRegionSliceResult;
import com.region.application.model.read.RegionResult;
import org.springframework.stereotype.Component;
import org.mapstruct.Mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.mapper.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public abstract class RegionQueryMapper {

    public abstract RegionSlice toSlice(FindRegionSliceResult slice);

    public abstract RegionSlice toSlice(RegionResult region);
}
