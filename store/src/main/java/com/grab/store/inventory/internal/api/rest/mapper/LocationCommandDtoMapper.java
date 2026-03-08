package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.AddBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AddZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationAddressResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.command.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class LocationCommandDtoMapper {

    @Mapping(target = "createdBy", source = "actorId")
    @Mapping(target = "line1", source = "request.address.line1")
    @Mapping(target = "line2", source = "request.address.line2")
    @Mapping(target = "city", source = "request.address.city")
    @Mapping(target = "state", source = "request.address.state")
    @Mapping(target = "postalCode", source = "request.address.postalCode")
    @Mapping(target = "country", source = "request.address.country")
    public abstract CreateLocationCommand toCreateCommand(CreateLocationRequest request, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "updatedBy", source = "actorId")
    @Mapping(target = "line1", source = "request.address.line1")
    @Mapping(target = "line2", source = "request.address.line2")
    @Mapping(target = "city", source = "request.address.city")
    @Mapping(target = "state", source = "request.address.state")
    @Mapping(target = "postalCode", source = "request.address.postalCode")
    @Mapping(target = "country", source = "request.address.country")
    @Mapping(target = "addressProvided", expression = "java(request.address() != null)")
    public abstract UpdateLocationCommand toUpdateCommand(String locationId, UpdateLocationRequest request, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "initiatedBy", source = "actorId")
    public abstract ActivateLocationCommand toActivateCommand(String locationId, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "initiatedBy", source = "actorId")
    public abstract DeactivateLocationCommand toDeactivateCommand(String locationId, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "createdBy", source = "actorId")
    public abstract AddZoneCommand toAddZoneCommand(String locationId, AddZoneRequest request, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "zoneId", source = "zoneId")
    @Mapping(target = "updatedBy", source = "actorId")
    public abstract UpdateZoneCommand toUpdateZoneCommand(String locationId, String zoneId, UpdateZoneRequest request, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "zoneId", source = "zoneId")
    @Mapping(target = "removedBy", source = "actorId")
    public abstract RemoveZoneCommand toRemoveZoneCommand(String locationId, String zoneId, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "zoneId", source = "zoneId")
    @Mapping(target = "createdBy", source = "actorId")
    public abstract AddBinCommand toAddBinCommand(String locationId, String zoneId, AddBinRequest request, String actorId);

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "zoneId", source = "zoneId")
    @Mapping(target = "binId", source = "binId")
    @Mapping(target = "updatedBy", source = "actorId")
    public abstract UpdateBinCommand toUpdateBinCommand(
            String locationId,
            String zoneId,
            String binId,
            UpdateBinRequest request,
            String actorId
    );

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "zoneId", source = "zoneId")
    @Mapping(target = "binId", source = "binId")
    @Mapping(target = "removedBy", source = "actorId")
    public abstract RemoveBinCommand toRemoveBinCommand(String locationId, String zoneId, String binId, String actorId);

    public abstract LocationResponse toResponse(LocationResult result);

    public abstract LocationAddressResponse toAddressResponse(LocationResult.Address address);

    public abstract ZoneResponse toZoneResponse(LocationResult.Zone zone);

    public abstract BinResponse toBinResponse(LocationResult.Bin bin);
}
