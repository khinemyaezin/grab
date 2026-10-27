package com.grab.app.platform.seller.internal.command;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.CreateLocationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.UpdateLocationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.LocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.ActivateLocationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.CreateLocationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.DeactivateLocationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.DeleteLocationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.UpdateLocationRequestMapper;
import com.inventory.application.model.write.ActivateLocationCommand;
import com.inventory.application.model.write.CreateLocationCommand;
import com.inventory.application.model.write.DeactivateLocationCommand;
import com.inventory.application.model.write.DeleteLocationCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.model.write.UpdateLocationCommand;
import lombok.RequiredArgsConstructor;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;
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
