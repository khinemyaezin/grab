package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateLocationRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import com.grab.store.inventory.internal.api.rest.mapper.ActivateLocationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.CreateLocationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.DeactivateLocationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.DeleteLocationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.UpdateLocationRequestMapper;
import com.grab.store.inventory.internal.command.ActivateLocationCommand;
import com.grab.store.inventory.internal.command.CreateLocationCommand;
import com.grab.store.inventory.internal.command.DeactivateLocationCommand;
import com.grab.store.inventory.internal.command.DeleteLocationCommand;
import com.grab.store.inventory.internal.command.LocationResult;
import com.grab.store.inventory.internal.command.UpdateLocationCommand;
import lombok.RequiredArgsConstructor;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationCommandService {

    private final CommandBus commandBus;
    private final CreateLocationRequestMapper createLocationRequestMapper;
    private final UpdateLocationRequestMapper updateLocationRequestMapper;
    private final ActivateLocationRequestMapper activateLocationRequestMapper;
    private final DeactivateLocationRequestMapper deactivateLocationRequestMapper;
    private final DeleteLocationRequestMapper deleteLocationRequestMapper;

    public LocationResponse createLocation(CreateLocationRequest request, String actorId) {
        CreateLocationCommand command = createLocationRequestMapper.toCommand(request, actorId);
        LocationResult result = commandBus.dispatch(command);
        return createLocationRequestMapper.toResponse(result);
    }

    public LocationResponse updateLocation(String locationId, UpdateLocationRequest request, ResolvedInventoryAccess access) {
        UpdateLocationCommand command = updateLocationRequestMapper.toCommand(locationId, request, access.actorId(), access.scopeKey(), access.scopeId());
        LocationResult result = commandBus.dispatch(command);
        return updateLocationRequestMapper.toResponse(result);
    }

    public LocationResponse activateLocation(String locationId, ResolvedInventoryAccess access) {
        ActivateLocationCommand command = activateLocationRequestMapper.toCommand(locationId, access.actorId(), access.scopeKey(), access.scopeId());
        LocationResult result = commandBus.dispatch(command);
        return activateLocationRequestMapper.toResponse(result);
    }

    public LocationResponse deactivateLocation(String locationId, ResolvedInventoryAccess access) {
        DeactivateLocationCommand command = deactivateLocationRequestMapper.toCommand(locationId, access.actorId(), access.scopeKey(), access.scopeId());
        LocationResult result = commandBus.dispatch(command);
        return deactivateLocationRequestMapper.toResponse(result);
    }

    public void deleteLocation(String locationId, ResolvedInventoryAccess access) {
        DeleteLocationCommand command = deleteLocationRequestMapper.toCommand(locationId, access.actorId(), access.scopeKey(), access.scopeId());
        commandBus.dispatch(command);
    }
}
