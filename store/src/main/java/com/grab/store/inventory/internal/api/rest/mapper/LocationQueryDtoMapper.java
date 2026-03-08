package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.inventory.domain.enums.LocationType;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationsResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.query.GetLocationByCodeQuery;
import com.grab.store.inventory.internal.query.GetLocationQuery;
import com.grab.store.inventory.internal.query.GetLocationResult;
import com.grab.store.inventory.internal.query.ListLocationsQuery;
import com.grab.store.inventory.internal.query.ListLocationsResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LocationQueryDtoMapper {

    public abstract GetLocationQuery toGetByIdQuery(String locationId);

    public abstract GetLocationByCodeQuery toGetByCodeQuery(String code);

    public abstract ListLocationsQuery toListQuery(Boolean active, LocationType type);

    public abstract LocationResponse toResponse(GetLocationResult result);

    public abstract LocationsResponse toResponse(ListLocationsResult result);

    public abstract LocationAddressResponse toAddressResponse(GetLocationResult.Address address);

    public abstract ZoneResponse toZoneResponse(GetLocationResult.Zone zone);

    public abstract BinResponse toBinResponse(GetLocationResult.Bin bin);
}
