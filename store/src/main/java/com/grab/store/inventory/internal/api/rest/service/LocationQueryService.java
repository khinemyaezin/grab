package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchLocationRequest;
import com.grab.store.inventory.internal.api.rest.mapper.GetLocationByCodeRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.GetLocationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ListLocationsRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.SearchLocationsRequestMapper;
import com.grab.store.inventory.internal.query.GetLocationByCodeQuery;
import com.grab.store.inventory.internal.query.GetLocationQuery;
import com.grab.store.inventory.internal.query.GetLocationResult;
import com.grab.store.inventory.internal.query.ListLocationsQuery;
import com.grab.store.inventory.internal.query.ListLocationsResult;
import com.grab.store.inventory.internal.query.SearchLocationsQuery;
import com.grab.store.inventory.internal.query.SearchLocationsResult;
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
    private final SearchLocationsRequestMapper searchLocationsRequestMapper;

    public LocationResponse getLocation(String locationId) {
        GetLocationQuery query = getLocationRequestMapper.toQuery(locationId);
        GetLocationResult result = queryBus.dispatch(query);
        return getLocationRequestMapper.toResponse(result);
    }

    public LocationResponse getLocationByCode(String code) {
        GetLocationByCodeQuery query = getLocationByCodeRequestMapper.toQuery(code);
        GetLocationResult result = queryBus.dispatch(query);
        return getLocationByCodeRequestMapper.toResponse(result);
    }

    public Page<LocationResponse> listLocations(String merchantId, Boolean active, LocationType type, Pageable pageable) {
        ListLocationsQuery query = listLocationsRequestMapper.toQuery(merchantId, active, type, pageable);
        Page<ListLocationsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listLocationsRequestMapper::toResponse);
    }

    public Page<LocationResponse> searchLocations(String merchantId, SearchLocationRequest request, Pageable pageable) {
        SearchLocationsQuery query = searchLocationsRequestMapper.toQuery(merchantId, request, pageable);
        Page<SearchLocationsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(searchLocationsRequestMapper::toResponse);
    }
}
