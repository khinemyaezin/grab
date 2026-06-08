package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.api.rest.mapper.GetZoneRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ListZonesRequestMapper;
import com.grab.store.inventory.internal.query.GetZoneQuery;
import com.grab.store.inventory.internal.query.GetZoneResult;
import com.grab.store.inventory.internal.query.ListZonesByLocationQuery;
import com.grab.store.inventory.internal.query.ListZonesResult;
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
}
