package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.inventory.domain.enums.LocationType;
import com.grab.store.inventory.internal.api.rest.assembler.LocationModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.LocationsModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationsResponse;
import com.grab.store.inventory.internal.api.rest.mapper.LocationQueryDtoMapper;
import com.grab.store.inventory.internal.query.GetLocationByCodeQuery;
import com.grab.store.inventory.internal.query.GetLocationQuery;
import com.grab.store.inventory.internal.query.GetLocationResult;
import com.grab.store.inventory.internal.query.ListLocationsQuery;
import com.grab.store.inventory.internal.query.ListLocationsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationQueryService {

    private final QueryBus queryBus;
    private final LocationQueryDtoMapper locationQueryDtoMapper;
    private final LocationModelAssembler locationModelAssembler;
    private final LocationsModelAssembler locationsModelAssembler;

    public EntityModel<LocationResponse> getLocation(String locationId) {
        GetLocationQuery query = locationQueryDtoMapper.toGetByIdQuery(locationId);
        GetLocationResult result = queryBus.dispatch(query);
        return locationModelAssembler.toModel(locationQueryDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> getLocationByCode(String code) {
        GetLocationByCodeQuery query = locationQueryDtoMapper.toGetByCodeQuery(code);
        GetLocationResult result = queryBus.dispatch(query);
        return locationModelAssembler.toModel(locationQueryDtoMapper.toResponse(result));
    }

    public EntityModel<LocationsResponse> listLocations(Boolean active, LocationType type) {
        ListLocationsQuery query = locationQueryDtoMapper.toListQuery(active, type);
        ListLocationsResult result = queryBus.dispatch(query);
        return locationsModelAssembler.toModel(locationQueryDtoMapper.toResponse(result));
    }
}
