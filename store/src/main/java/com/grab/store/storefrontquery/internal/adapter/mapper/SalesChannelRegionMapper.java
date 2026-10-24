package com.grab.store.storefrontquery.internal.adapter.mapper;

import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.storefrontquery.internal.api.rest.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.Optional;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SalesChannelRegionMapper {
    public Optional<Id> toDefaultRegion(Optional<Id> defaultRegion) {
        return defaultRegion;
    }
}
