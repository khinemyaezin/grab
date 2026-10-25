package com.grab.app.channeldiscovery.storefrontquery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.framework.mapper.IdMapper;
import com.grab.app.channeldiscovery.region.port.RegionQuery;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class RegionContextMapper {
    @Mapping(target = "regionId", source = "regionId")
    @Mapping(target = "active", expression = "java(slice.active())")
    public abstract RegionContextPort.RegionView toRegionView(RegionQuery.RegionSlice slice);
}
