package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.query.ListLocationsQuery;
import com.grab.store.inventory.internal.query.ListLocationsResult;
import com.inventory.domain.enums.LocationType;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListLocationsRequestMapper {

    public abstract ListLocationsQuery toQuery(String sellerId, Boolean active, LocationType type, Pageable pageable);

    public abstract LocationResponse toResponse(ListLocationsResult result);

    protected LocationAddressResponse mapAddress(ListLocationsResult.Address address) {
        if (address == null) return null;
        return new LocationAddressResponse(
                address.line1(), address.line2(), address.city(),
                address.state(), address.postalCode(), address.country());
    }
}
