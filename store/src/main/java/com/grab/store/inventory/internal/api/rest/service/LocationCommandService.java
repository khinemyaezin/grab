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
        var command = createLocationRequestMapper.toCommand(request, actorId);
        var result = commandBus.dispatch(command);
        return createLocationRequestMapper.toResponse(result);
    }

    public LocationResponse updateLocation(String locationId, UpdateLocationRequest request, ResolvedInventoryAccess access) {
        var command = updateLocationRequestMapper.toCommand(locationId, request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return updateLocationRequestMapper.toResponse(result);
    }

    public LocationResponse activateLocation(String locationId, ResolvedInventoryAccess access) {
        var command = activateLocationRequestMapper.toCommand(locationId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return activateLocationRequestMapper.toResponse(result);
    }

    public LocationResponse deactivateLocation(String locationId, ResolvedInventoryAccess access) {
        var command = deactivateLocationRequestMapper.toCommand(locationId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return deactivateLocationRequestMapper.toResponse(result);
    }

    public void deleteLocation(String locationId, ResolvedInventoryAccess access) {
        var command = deleteLocationRequestMapper.toCommand(locationId, access.actorId(), access.scopeKey(), access.scopeId());
        commandBus.dispatch(command);
    }
}
