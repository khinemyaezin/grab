package com.grab.store.region.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.region.internal.api.rest.dto.response.RegionResponse;
import com.region.application.model.read.GetRegionQuery;
import com.region.application.model.read.RegionResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetRegionRequestMapper {

    public GetRegionQuery toQuery(String regionId) {
        return new GetRegionQuery(regionId);
    }

    public abstract RegionResponse toResponse(RegionResult result);
}
