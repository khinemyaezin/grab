package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.CreateZoneRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.UpdateZoneRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.ZoneResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.ActivateZoneRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.CreateZoneRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.DeactivateZoneRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.DeleteZoneRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.UpdateZoneRequestMapper;
import com.inventory.application.model.write.ActivateZoneCommand;
import com.inventory.application.model.write.CreateZoneCommand;
import com.inventory.application.model.write.DeactivateZoneCommand;
import com.inventory.application.model.write.DeleteZoneCommand;
import com.inventory.application.model.write.UpdateZoneCommand;
import com.inventory.application.model.write.ZoneResult;
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
