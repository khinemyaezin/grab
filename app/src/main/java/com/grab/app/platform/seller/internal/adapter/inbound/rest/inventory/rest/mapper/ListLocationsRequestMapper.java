package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.LocationAddressResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.LocationResponse;
import com.inventory.application.model.read.ListLocationsQuery;
import com.inventory.application.model.read.ListLocationsResult;
import com.inventory.domain.enums.LocationType;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListLocationsRequestMapper {

    public abstract ListLocationsQuery toQuery(String merchantId, Boolean active, LocationType type, Pageable pageable);

    public abstract LocationResponse toResponse(ListLocationsResult result);

    protected LocationAddressResponse mapAddress(ListLocationsResult.Address address) {
        if (address == null) return null;
        return new LocationAddressResponse(
                address.line1(), address.line2(), address.city(),
                address.state(), address.postalCode(), address.country());
    }
}
