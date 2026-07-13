package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateZoneRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.inventory.internal.api.rest.mapper.ActivateZoneRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.CreateZoneRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.DeactivateZoneRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.DeleteZoneRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.UpdateZoneRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZoneCommandService {

    private final CommandBus commandBus;
    private final CreateZoneRequestMapper createZoneRequestMapper;
    private final UpdateZoneRequestMapper updateZoneRequestMapper;
    private final ActivateZoneRequestMapper activateZoneRequestMapper;
    private final DeactivateZoneRequestMapper deactivateZoneRequestMapper;
    private final DeleteZoneRequestMapper deleteZoneRequestMapper;

    public ZoneResponse createZone(String locationId, CreateZoneRequest request, ResolvedInventoryAccess access) {
        var command = createZoneRequestMapper.toCommand(
                locationId, request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return createZoneRequestMapper.toResponse(result);
    }

    public ZoneResponse updateZone(String zoneId, UpdateZoneRequest request, ResolvedInventoryAccess access) {
        var command = updateZoneRequestMapper.toCommand(
                zoneId, request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return updateZoneRequestMapper.toResponse(result);
    }

    public ZoneResponse activateZone(String zoneId, ResolvedInventoryAccess access) {
        var command = activateZoneRequestMapper.toCommand(
                zoneId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return activateZoneRequestMapper.toResponse(result);
    }

    public ZoneResponse deactivateZone(String zoneId, ResolvedInventoryAccess access) {
        var command = deactivateZoneRequestMapper.toCommand(
                zoneId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return deactivateZoneRequestMapper.toResponse(result);
    }

    public void deleteZone(String zoneId, ResolvedInventoryAccess access) {
        var command = deleteZoneRequestMapper.toCommand(
                zoneId, access.actorId(), access.scopeKey(), access.scopeId());
        commandBus.dispatch(command);
    }
}
