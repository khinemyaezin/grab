package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.query.ListZonesByLocationQuery;
import com.grab.store.inventory.internal.query.ListZonesResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListZonesRequestMapper {

    public abstract ListZonesByLocationQuery toQuery(String locationId, Boolean active);

    public abstract ZoneResponse toResponse(ListZonesResult result);
}
