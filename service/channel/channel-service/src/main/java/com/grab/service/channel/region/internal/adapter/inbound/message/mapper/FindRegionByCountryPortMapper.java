package com.grab.service.channel.region.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.channel.region.RegionPort.RegionMessageResult;
import com.grab.service.channel.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.region.application.model.read.FindRegionByCountryQuery;
import com.region.application.model.read.RegionResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class FindRegionByCountryPortMapper {

    public FindRegionByCountryQuery toQuery(String countryCode) {
        return countryCode != null ? new FindRegionByCountryQuery(countryCode) : null;
    }

    public abstract RegionMessageResult toResponse(RegionResult result);
}

