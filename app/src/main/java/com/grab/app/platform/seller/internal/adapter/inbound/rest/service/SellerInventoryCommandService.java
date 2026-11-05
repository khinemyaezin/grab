package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.AllocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityLocationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityZoneRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateInventoryItemRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.DeallocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ActivateSellerBinMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ActivateSellerLocationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ActivateSellerZoneMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.AllocateSellerStockMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerBinMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerInventoryItemMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerLocationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CreateSellerZoneMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeactivateSellerBinMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeactivateSellerLocationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeactivateSellerZoneMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeallocateSellerStockMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeleteSellerBinMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeleteSellerLocationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.DeleteSellerZoneMapper;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.ActivateSellerBinCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.ActivateSellerLocationCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.ActivateSellerZoneCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.AllocateSellerStockCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerBinCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerInventoryItemCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerLocationCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerZoneCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeactivateSellerBinCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeactivateSellerLocationCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeactivateSellerZoneCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeallocateSellerStockCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeleteSellerBinCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeleteSellerLocationCommand;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeleteSellerZoneCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.AllocationOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.framework.cqrs.command.CommandBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SellerInventoryCommandService {

    private final CommandBus commandBus;
    private final CreateSellerInventoryItemMapper createInventoryItemMapper;
    private final CreateSellerLocationMapper createLocationMapper;
    private final ActivateSellerLocationMapper activateLocationMapper;
    private final DeactivateSellerLocationMapper deactivateLocationMapper;
    private final DeleteSellerLocationMapper deleteLocationMapper;
    private final CreateSellerZoneMapper createZoneMapper;
    private final ActivateSellerZoneMapper activateZoneMapper;
    private final DeactivateSellerZoneMapper deactivateZoneMapper;
    private final DeleteSellerZoneMapper deleteZoneMapper;
    private final CreateSellerBinMapper createBinMapper;
    private final ActivateSellerBinMapper activateBinMapper;
    private final DeactivateSellerBinMapper deactivateBinMapper;
    private final DeleteSellerBinMapper deleteBinMapper;
    private final AllocateSellerStockMapper allocateStockMapper;
    private final DeallocateSellerStockMapper deallocateStockMapper;

    public SellerInventoryCommandService(
            @Qualifier("sellerCommandBus") CommandBus commandBus,
            CreateSellerInventoryItemMapper createInventoryItemMapper,
            CreateSellerLocationMapper createLocationMapper,
            ActivateSellerLocationMapper activateLocationMapper,
            DeactivateSellerLocationMapper deactivateLocationMapper,
            DeleteSellerLocationMapper deleteLocationMapper,
            CreateSellerZoneMapper createZoneMapper,
            ActivateSellerZoneMapper activateZoneMapper,
            DeactivateSellerZoneMapper deactivateZoneMapper,
            DeleteSellerZoneMapper deleteZoneMapper,
            CreateSellerBinMapper createBinMapper,
            ActivateSellerBinMapper activateBinMapper,
            DeactivateSellerBinMapper deactivateBinMapper,
            DeleteSellerBinMapper deleteBinMapper,
            AllocateSellerStockMapper allocateStockMapper,
            DeallocateSellerStockMapper deallocateStockMapper
    ) {
        this.commandBus = commandBus;
        this.createInventoryItemMapper = createInventoryItemMapper;
        this.createLocationMapper = createLocationMapper;
        this.activateLocationMapper = activateLocationMapper;
        this.deactivateLocationMapper = deactivateLocationMapper;
        this.deleteLocationMapper = deleteLocationMapper;
        this.createZoneMapper = createZoneMapper;
        this.activateZoneMapper = activateZoneMapper;
        this.deactivateZoneMapper = deactivateZoneMapper;
        this.deleteZoneMapper = deleteZoneMapper;
        this.createBinMapper = createBinMapper;
        this.activateBinMapper = activateBinMapper;
        this.deactivateBinMapper = deactivateBinMapper;
        this.deleteBinMapper = deleteBinMapper;
        this.allocateStockMapper = allocateStockMapper;
        this.deallocateStockMapper = deallocateStockMapper;
    }

    public InventoryItemResponse createItem(String merchantIdStr, String actorIdStr, CreateInventoryItemRequest request) {
        CreateSellerInventoryItemCommand cmd = createInventoryItemMapper.toCommand(merchantIdStr, actorIdStr, request);
        InventoryItemOutboundResult result = commandBus.dispatch(cmd);
        return createInventoryItemMapper.toResponse(result);
    }

    public FacilityLocationResponse createLocation(String merchantIdStr, String actorIdStr, CreateFacilityLocationRequest request) {
        CreateSellerLocationCommand cmd = createLocationMapper.toCommand(merchantIdStr, actorIdStr, request);
        FacilityLocationOutboundResult result = commandBus.dispatch(cmd);
        return createLocationMapper.toResponse(result);
    }

    public void activateLocation(String locationIdStr) {
        ActivateSellerLocationCommand cmd = activateLocationMapper.toCommand(locationIdStr);
        commandBus.dispatch(cmd);
    }

    public void deactivateLocation(String locationIdStr) {
        DeactivateSellerLocationCommand cmd = deactivateLocationMapper.toCommand(locationIdStr);
        commandBus.dispatch(cmd);
    }

    public void deleteLocation(String locationIdStr) {
        DeleteSellerLocationCommand cmd = deleteLocationMapper.toCommand(locationIdStr);
        commandBus.dispatch(cmd);
    }

    public FacilityZoneResponse createZone(String locationIdStr, String actorIdStr, CreateFacilityZoneRequest request) {
        CreateSellerZoneCommand cmd = createZoneMapper.toCommand(locationIdStr, actorIdStr, request);
        FacilityZoneOutboundResult result = commandBus.dispatch(cmd);
        return createZoneMapper.toResponse(result);
    }

    public void activateZone(String zoneIdStr) {
        ActivateSellerZoneCommand cmd = activateZoneMapper.toCommand(zoneIdStr);
        commandBus.dispatch(cmd);
    }

    public void deactivateZone(String zoneIdStr) {
        DeactivateSellerZoneCommand cmd = deactivateZoneMapper.toCommand(zoneIdStr);
        commandBus.dispatch(cmd);
    }

    public void deleteZone(String zoneIdStr) {
        DeleteSellerZoneCommand cmd = deleteZoneMapper.toCommand(zoneIdStr);
        commandBus.dispatch(cmd);
    }

    public FacilityBinResponse createBin(String zoneIdStr, String actorIdStr, CreateFacilityBinRequest request) {
        CreateSellerBinCommand cmd = createBinMapper.toCommand(zoneIdStr, actorIdStr, request);
        FacilityBinOutboundResult result = commandBus.dispatch(cmd);
        return createBinMapper.toResponse(result);
    }

    public void activateBin(String binIdStr) {
        ActivateSellerBinCommand cmd = activateBinMapper.toCommand(binIdStr);
        commandBus.dispatch(cmd);
    }

    public void deactivateBin(String binIdStr) {
        DeactivateSellerBinCommand cmd = deactivateBinMapper.toCommand(binIdStr);
        commandBus.dispatch(cmd);
    }

    public void deleteBin(String binIdStr) {
        DeleteSellerBinCommand cmd = deleteBinMapper.toCommand(binIdStr);
        commandBus.dispatch(cmd);
    }

    public AllocationResponse allocateStock(String actorIdStr, AllocateStockRequest request) {
        AllocateSellerStockCommand cmd = allocateStockMapper.toCommand(actorIdStr, request);
        AllocationOutboundResult result = commandBus.dispatch(cmd);
        return allocateStockMapper.toResponse(result);
    }

    public void deallocateStock(String actorIdStr, DeallocateStockRequest request) {
        DeallocateSellerStockCommand cmd = deallocateStockMapper.toCommand(actorIdStr, request);
        commandBus.dispatch(cmd);
    }
}
