package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.query.ListBinsByZoneQuery;
import com.grab.store.inventory.internal.query.ListBinsResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListBinsRequestMapper {

    public abstract ListBinsByZoneQuery toQuery(String zoneId, Boolean active, Pageable pageable);

    public abstract BinResponse toResponse(ListBinsResult result);
}
