package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.api.rest.mapper.GetZoneRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ListZonesRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.SearchZonesRequestMapper;
import com.grab.store.inventory.internal.query.GetZoneQuery;
import com.grab.store.inventory.internal.query.GetZoneLocationIdQuery;
import com.grab.store.inventory.internal.query.GetZoneResult;
import com.grab.store.inventory.internal.query.ListZonesByLocationQuery;
import com.grab.store.inventory.internal.query.ListZonesResult;
import com.grab.store.inventory.internal.query.SearchZonesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZoneQueryService {

    private final QueryBus queryBus;
    private final GetZoneRequestMapper getZoneRequestMapper;
    private final ListZonesRequestMapper listZonesRequestMapper;
    private final SearchZonesRequestMapper searchZonesRequestMapper;
    private final IdGenerator idGenerator;

    public ZoneResponse getZone(String zoneId) {
        GetZoneQuery query = getZoneRequestMapper.toQuery(zoneId);
        GetZoneResult result = queryBus.dispatch(query);
        return getZoneRequestMapper.toResponse(result);
    }

    public Page<ZoneResponse> listZones(String locationId, Pageable pageable) {
        ListZonesByLocationQuery query = listZonesRequestMapper.toQuery(locationId, pageable);
        Page<ListZonesResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listZonesRequestMapper::toResponse);
    }

    public Page<ZoneResponse> searchZones(String merchantId, SearchZoneRequest request, Pageable pageable) {
        var query = searchZonesRequestMapper.toQuery(merchantId, request, pageable);
        Page<SearchZonesResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(searchZonesRequestMapper::toResponse);
    }

    public String getLocationId(String zoneId) {
        var query = new GetZoneLocationIdQuery(idGenerator.convertIdFrom(zoneId));
        return queryBus.dispatch(query);
    }
}
