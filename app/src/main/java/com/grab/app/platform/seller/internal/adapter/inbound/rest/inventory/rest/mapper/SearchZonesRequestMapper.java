package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.SearchZoneRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.ZoneResponse;
import com.inventory.application.model.read.SearchZonesQuery;
import com.inventory.application.model.read.SearchZonesResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchZonesRequestMapper {

    public abstract SearchZonesQuery toQuery(String merchantId, SearchZoneRequest request, Pageable pageable);

    public abstract ZoneResponse toResponse(SearchZonesResult result);
}
