package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.UpdateLocationCommand;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateLocationRequestMapper {

    @Mapping(target = "line1", source = "request.address.line1")
    @Mapping(target = "line2", source = "request.address.line2")
    @Mapping(target = "city", source = "request.address.city")
    @Mapping(target = "state", source = "request.address.state")
    @Mapping(target = "postalCode", source = "request.address.postalCode")
    @Mapping(target = "country", source = "request.address.country")
    @Mapping(target = "addressProvided", expression = "java(request.address() != null)")
    public abstract UpdateLocationCommand toCommand(String locationId, UpdateLocationRequest request, String updatedBy);

    public abstract LocationResponse toResponse(LocationResult result);

    protected LocationAddressResponse mapAddress(LocationResult.Address address) {
        if (address == null) return null;
        return new LocationAddressResponse(
                address.line1(), address.line2(), address.city(),
                address.state(), address.postalCode(), address.country());
    }
}
