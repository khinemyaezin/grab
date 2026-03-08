package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.inventory.internal.api.rest.assembler.LocationModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AddBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AddZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.mapper.LocationCommandDtoMapper;
import com.grab.store.inventory.internal.command.*;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationCommandService {

    private final CommandBus commandBus;
    private final LocationCommandDtoMapper locationCommandDtoMapper;
    private final LocationModelAssembler locationModelAssembler;

    public EntityModel<LocationResponse> createLocation(CreateLocationRequest request, String actorId) {
        CreateLocationCommand command = locationCommandDtoMapper.toCreateCommand(request, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> updateLocation(String locationId, UpdateLocationRequest request, String actorId) {
        UpdateLocationCommand command = locationCommandDtoMapper.toUpdateCommand(locationId, request, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> activateLocation(String locationId, String actorId) {
        ActivateLocationCommand command = locationCommandDtoMapper.toActivateCommand(locationId, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> deactivateLocation(String locationId, String actorId) {
        DeactivateLocationCommand command = locationCommandDtoMapper.toDeactivateCommand(locationId, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> addZone(String locationId, AddZoneRequest request, String actorId) {
        AddZoneCommand command = locationCommandDtoMapper.toAddZoneCommand(locationId, request, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> updateZone(String locationId, String zoneId, UpdateZoneRequest request, String actorId) {
        UpdateZoneCommand command = locationCommandDtoMapper.toUpdateZoneCommand(locationId, zoneId, request, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> removeZone(String locationId, String zoneId, String actorId) {
        RemoveZoneCommand command = locationCommandDtoMapper.toRemoveZoneCommand(locationId, zoneId, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> addBin(String locationId, String zoneId, AddBinRequest request, String actorId) {
        AddBinCommand command = locationCommandDtoMapper.toAddBinCommand(locationId, zoneId, request, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> updateBin(
            String locationId,
            String zoneId,
            String binId,
            UpdateBinRequest request,
            String actorId
    ) {
        UpdateBinCommand command = locationCommandDtoMapper.toUpdateBinCommand(locationId, zoneId, binId, request, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }

    public EntityModel<LocationResponse> removeBin(String locationId, String zoneId, String binId, String actorId) {
        RemoveBinCommand command = locationCommandDtoMapper.toRemoveBinCommand(locationId, zoneId, binId, actorId);
        LocationResult result = commandBus.dispatch(command);
        return locationModelAssembler.toModel(locationCommandDtoMapper.toResponse(result));
    }
}
