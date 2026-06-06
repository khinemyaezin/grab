package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.query.GetLocationByCodeQuery;
import com.grab.store.inventory.internal.query.GetLocationResult;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetLocationByCodeRequestMapper {

    public abstract GetLocationByCodeQuery toQuery(String code);

    public abstract LocationResponse toResponse(GetLocationResult result);

    protected LocationAddressResponse mapAddress(GetLocationResult.Address address) {
        if (address == null) return null;
        return new LocationAddressResponse(
                address.line1(), address.line2(), address.city(),
                address.state(), address.postalCode(), address.country());
    }
}
