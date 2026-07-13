package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.mapper.GetLocationByCodeRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetLocationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ListLocationsRequestMapper;
import com.grab.store.inventory.internal.query.ListLocationsResult;
import com.inventory.domain.enums.LocationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationQueryService {

    private final QueryBus queryBus;
    private final GetLocationRequestMapper getLocationRequestMapper;
    private final GetLocationByCodeRequestMapper getLocationByCodeRequestMapper;
    private final ListLocationsRequestMapper listLocationsRequestMapper;

    public LocationResponse getLocation(String locationId) {
        var query = getLocationRequestMapper.toQuery(locationId);
        var result = queryBus.dispatch(query);
        return getLocationRequestMapper.toResponse(result);
    }

    public LocationResponse getLocationByCode(String code) {
        var query = getLocationByCodeRequestMapper.toQuery(code);
        var result = queryBus.dispatch(query);
        return getLocationByCodeRequestMapper.toResponse(result);
    }

    public Page<LocationResponse> listLocations(String merchantId, Boolean active, LocationType type, Pageable pageable) {
        var query = listLocationsRequestMapper.toQuery(merchantId, active, type, pageable);
        Page<ListLocationsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listLocationsRequestMapper::toResponse);
    }
}
