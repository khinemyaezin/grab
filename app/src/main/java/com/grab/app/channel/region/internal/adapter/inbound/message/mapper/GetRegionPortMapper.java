package com.grab.app.channel.region.internal.adapter.inbound.message.mapper;

import com.grab.app.channel.region.port.message.RegionPort.RegionMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.region.application.model.read.GetRegionQuery;
import com.region.application.model.read.RegionResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetRegionPortMapper {

    public GetRegionQuery toQuery(Id regionId) {
        return regionId != null ? new GetRegionQuery(regionId.getValue()) : null;
    }

    public abstract RegionMessageResult toResponse(RegionResult result);
}

