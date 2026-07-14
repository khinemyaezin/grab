package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.query.SearchLocationsQuery;
import com.grab.store.inventory.internal.query.SearchLocationsResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchLocationsRequestMapper {

    public abstract SearchLocationsQuery toQuery(String merchantId, SearchLocationRequest request, Pageable pageable);

    public abstract LocationResponse toResponse(SearchLocationsResult result);

    protected LocationAddressResponse mapAddress(SearchLocationsResult.Address address) {
        if (address == null) return null;
        return new LocationAddressResponse(
                address.line1(), address.line2(), address.city(),
                address.state(), address.postalCode(), address.country());
    }
}
