package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.query.GetZoneQuery;
import com.grab.store.inventory.internal.query.GetZoneResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetZoneRequestMapper {

    public abstract GetZoneQuery toQuery(String zoneId);

    public abstract ZoneResponse toResponse(GetZoneResult result);
}
