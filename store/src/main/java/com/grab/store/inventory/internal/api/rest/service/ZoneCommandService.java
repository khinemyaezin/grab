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
import com.grab.store.inventory.internal.command.ActivateZoneCommand;
import com.grab.store.inventory.internal.command.CreateZoneCommand;
import com.grab.store.inventory.internal.command.DeactivateZoneCommand;
import com.grab.store.inventory.internal.command.DeleteZoneCommand;
import com.grab.store.inventory.internal.command.UpdateZoneCommand;
import com.grab.store.inventory.internal.command.ZoneResult;
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
        CreateZoneCommand command = createZoneRequestMapper.toCommand(
                locationId, request, access.actorId(), access.scopeKey(), access.scopeId());
        ZoneResult result = commandBus.dispatch(command);
        return createZoneRequestMapper.toResponse(result);
    }

    public ZoneResponse updateZone(String zoneId, UpdateZoneRequest request, ResolvedInventoryAccess access) {
        UpdateZoneCommand command = updateZoneRequestMapper.toCommand(
                zoneId, request, access.actorId(), access.scopeKey(), access.scopeId());
        ZoneResult result = commandBus.dispatch(command);
        return updateZoneRequestMapper.toResponse(result);
    }

    public ZoneResponse activateZone(String zoneId, ResolvedInventoryAccess access) {
        ActivateZoneCommand command = activateZoneRequestMapper.toCommand(
                zoneId, access.actorId(), access.scopeKey(), access.scopeId());
        ZoneResult result = commandBus.dispatch(command);
        return activateZoneRequestMapper.toResponse(result);
    }

    public ZoneResponse deactivateZone(String zoneId, ResolvedInventoryAccess access) {
        DeactivateZoneCommand command = deactivateZoneRequestMapper.toCommand(
                zoneId, access.actorId(), access.scopeKey(), access.scopeId());
        ZoneResult result = commandBus.dispatch(command);
        return deactivateZoneRequestMapper.toResponse(result);
    }

    public void deleteZone(String zoneId, ResolvedInventoryAccess access) {
        DeleteZoneCommand command = deleteZoneRequestMapper.toCommand(
                zoneId, access.actorId(), access.scopeKey(), access.scopeId());
        commandBus.dispatch(command);
    }
}
