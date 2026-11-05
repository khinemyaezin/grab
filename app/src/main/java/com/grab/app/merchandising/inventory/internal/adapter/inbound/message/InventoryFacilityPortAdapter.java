package com.grab.app.merchandising.inventory.internal.adapter.inbound.message;

import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.CreateBinPortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.CreateLocationPortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.CreateZonePortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.GetBinPortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.GetLocationPortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.GetZonePortMapper;
import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.inventory.application.model.read.GetBinQuery;
import com.inventory.application.model.read.GetBinResult;
import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.model.read.GetLocationResult;
import com.inventory.application.model.read.GetZoneQuery;
import com.inventory.application.model.read.GetZoneResult;
import com.inventory.application.model.write.ActivateBinCommand;
import com.inventory.application.model.write.ActivateLocationCommand;
import com.inventory.application.model.write.ActivateZoneCommand;
import com.inventory.application.model.write.BinResult;
import com.inventory.application.model.write.CreateBinCommand;
import com.inventory.application.model.write.CreateLocationCommand;
import com.inventory.application.model.write.CreateZoneCommand;
import com.inventory.application.model.write.DeactivateBinCommand;
import com.inventory.application.model.write.DeactivateLocationCommand;
import com.inventory.application.model.write.DeactivateZoneCommand;
import com.inventory.application.model.write.DeleteBinCommand;
import com.inventory.application.model.write.DeleteLocationCommand;
import com.inventory.application.model.write.DeleteZoneCommand;
import com.inventory.application.model.write.LocationResult;
import com.inventory.application.model.write.ZoneResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventoryFacilityPortAdapter implements InventoryFacilityPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateLocationPortMapper createLocationMapper;
    private final GetLocationPortMapper getLocationMapper;
    private final CreateZonePortMapper createZoneMapper;
    private final GetZonePortMapper getZoneMapper;
    private final CreateBinPortMapper createBinMapper;
    private final GetBinPortMapper getBinMapper;

    public InventoryFacilityPortAdapter(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            CreateLocationPortMapper createLocationMapper,
            GetLocationPortMapper getLocationMapper,
            CreateZonePortMapper createZoneMapper,
            GetZonePortMapper getZoneMapper,
            CreateBinPortMapper createBinMapper,
            GetBinPortMapper getBinMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createLocationMapper = createLocationMapper;
        this.getLocationMapper = getLocationMapper;
        this.createZoneMapper = createZoneMapper;
        this.getZoneMapper = getZoneMapper;
        this.createBinMapper = createBinMapper;
        this.getBinMapper = getBinMapper;
    }

    @Override
    public FacilityLocationResult createLocation(CreateLocationMessage message) {
        CreateLocationCommand command = createLocationMapper.toCommand(message);
        LocationResult result = commandBus.dispatch(command);
        return createLocationMapper.toResponse(result);
    }

    @Override
    public void activateLocation(Id locationId) {
        ActivateLocationCommand command = new ActivateLocationCommand(locationId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public void deactivateLocation(Id locationId) {
        DeactivateLocationCommand command = new DeactivateLocationCommand(locationId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public void deleteLocation(Id locationId) {
        DeleteLocationCommand command = new DeleteLocationCommand(locationId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public Optional<FacilityLocationResult> findLocationById(Id locationId) {
        if (locationId == null) {
            return Optional.empty();
        }
        GetLocationQuery query = getLocationMapper.toQuery(locationId);
        GetLocationResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getLocationMapper.toResponse(result));
    }

    @Override
    public FacilityZoneResult createZone(Id locationId, CreateZoneMessage message) {
        CreateZoneCommand command = createZoneMapper.toCommand(locationId, message);
        ZoneResult result = commandBus.dispatch(command);
        return createZoneMapper.toResponse(result);
    }

    @Override
    public void activateZone(Id zoneId) {
        ActivateZoneCommand command = new ActivateZoneCommand(zoneId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public void deactivateZone(Id zoneId) {
        DeactivateZoneCommand command = new DeactivateZoneCommand(zoneId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public void deleteZone(Id zoneId) {
        DeleteZoneCommand command = new DeleteZoneCommand(zoneId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public Optional<FacilityZoneResult> findZoneById(Id zoneId) {
        if (zoneId == null) {
            return Optional.empty();
        }
        GetZoneQuery query = getZoneMapper.toQuery(zoneId);
        GetZoneResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getZoneMapper.toResponse(result));
    }

    @Override
    public FacilityBinResult createBin(Id zoneId, CreateBinMessage message) {
        CreateBinCommand command = createBinMapper.toCommand(zoneId, message);
        BinResult result = commandBus.dispatch(command);
        return createBinMapper.toResponse(result);
    }

    @Override
    public void activateBin(Id binId) {
        ActivateBinCommand command = new ActivateBinCommand(binId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public void deactivateBin(Id binId) {
        DeactivateBinCommand command = new DeactivateBinCommand(binId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public void deleteBin(Id binId) {
        DeleteBinCommand command = new DeleteBinCommand(binId, null, null, null);
        commandBus.dispatch(command);
    }

    @Override
    public Optional<FacilityBinResult> findBinById(Id binId) {
        if (binId == null) {
            return Optional.empty();
        }
        GetBinQuery query = getBinMapper.toQuery(binId);
        GetBinResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getBinMapper.toResponse(result));
    }
}
