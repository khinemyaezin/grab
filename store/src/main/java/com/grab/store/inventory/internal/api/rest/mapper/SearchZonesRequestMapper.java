package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.query.SearchZonesQuery;
import com.grab.store.inventory.internal.query.SearchZonesResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchZonesRequestMapper {

    public abstract SearchZonesQuery toQuery(String merchantId, SearchZoneRequest request, Pageable pageable);

    public abstract ZoneResponse toResponse(SearchZonesResult result);
}
