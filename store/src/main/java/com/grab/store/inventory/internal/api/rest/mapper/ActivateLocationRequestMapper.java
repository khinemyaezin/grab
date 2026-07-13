package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.command.ActivateLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ActivateLocationRequestMapper {

    public abstract ActivateLocationCommand toCommand(String locationId, String initiatedBy, String scopeKey, String scopeId);

    public abstract LocationResponse toResponse(LocationResult result);

    protected LocationAddressResponse mapAddress(LocationResult.Address address) {
        if (address == null) return null;
        return new LocationAddressResponse(
                address.line1(), address.line2(), address.city(),
                address.state(), address.postalCode(), address.country());
    }
}
