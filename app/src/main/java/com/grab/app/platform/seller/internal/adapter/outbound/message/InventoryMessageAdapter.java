package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort;
import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort;
import com.grab.app.merchandising.inventory.port.message.InventoryItemPort;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.AllocateStockMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreateFacilityBinMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreateFacilityLocationMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreateFacilityZoneMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreateInventoryItemMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.DeallocateStockMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetFacilityBinMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetFacilityLocationMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetFacilityZoneMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetInventoryItemMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetStockMovementMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListReorderSuggestionsMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InventoryMessageAdapter implements SellerInventoryOutboundPort {

    private final InventoryItemPort itemPort;
    private final InventoryFacilityPort facilityPort;
    private final InventoryAvailabilityPort availabilityPort;
    private final IdGenerator idGenerator;

    private final CreateInventoryItemMessageMapper createItemMapper;
    private final GetInventoryItemMessageMapper getItemMapper;
    private final GetStockMovementMessageMapper getMovementMapper;
    private final AllocateStockMessageMapper allocateStockMapper;
    private final DeallocateStockMessageMapper deallocateStockMapper;
    private final ListReorderSuggestionsMessageMapper listReorderSuggestionsMapper;
    private final CreateFacilityLocationMessageMapper createLocationMapper;
    private final GetFacilityLocationMessageMapper getLocationMapper;
    private final CreateFacilityZoneMessageMapper createZoneMapper;
    private final GetFacilityZoneMessageMapper getZoneMapper;
    private final CreateFacilityBinMessageMapper createBinMapper;
    private final GetFacilityBinMessageMapper getBinMapper;

    public InventoryMessageAdapter(
            InventoryItemPort itemPort,
            InventoryFacilityPort facilityPort,
            InventoryAvailabilityPort availabilityPort,
            IdGenerator idGenerator,
            CreateInventoryItemMessageMapper createItemMapper,
            GetInventoryItemMessageMapper getItemMapper,
            GetStockMovementMessageMapper getMovementMapper,
            AllocateStockMessageMapper allocateStockMapper,
            DeallocateStockMessageMapper deallocateStockMapper,
            ListReorderSuggestionsMessageMapper listReorderSuggestionsMapper,
            CreateFacilityLocationMessageMapper createLocationMapper,
            GetFacilityLocationMessageMapper getLocationMapper,
            CreateFacilityZoneMessageMapper createZoneMapper,
            GetFacilityZoneMessageMapper getZoneMapper,
            CreateFacilityBinMessageMapper createBinMapper,
            GetFacilityBinMessageMapper getBinMapper
    ) {
        this.itemPort = itemPort;
        this.facilityPort = facilityPort;
        this.availabilityPort = availabilityPort;
        this.idGenerator = idGenerator;
        this.createItemMapper = createItemMapper;
        this.getItemMapper = getItemMapper;
        this.getMovementMapper = getMovementMapper;
        this.allocateStockMapper = allocateStockMapper;
        this.deallocateStockMapper = deallocateStockMapper;
        this.listReorderSuggestionsMapper = listReorderSuggestionsMapper;
        this.createLocationMapper = createLocationMapper;
        this.getLocationMapper = getLocationMapper;
        this.createZoneMapper = createZoneMapper;
        this.getZoneMapper = getZoneMapper;
        this.createBinMapper = createBinMapper;
        this.getBinMapper = getBinMapper;
    }

    @Override
    public InventoryItemOutboundResult createItem(CreateItemRecord record) {
        InventoryItemPort.CreateInventoryItemMessage message = createItemMapper.toMessage(record);
        InventoryItemPort.ItemMessageResult result = itemPort.createInventoryItem(message);
        return createItemMapper.toResult(result);
    }

    @Override
    public Optional<InventoryItemOutboundResult> findItemById(String itemIdStr) {
        Id itemId = idGenerator.convertIdFrom(itemIdStr);
        return itemPort.findItemById(itemId).map(getItemMapper::toResult);
    }

    @Override
    public Optional<StockMovementOutboundResult> findLatestMovement(String itemIdStr) {
        Id itemId = idGenerator.convertIdFrom(itemIdStr);
        return itemPort.findLatestMovement(itemId).map(getMovementMapper::toResult);
    }

    @Override
    public AllocationOutboundResult allocateStock(AllocateStockRecord record) {
        InventoryAvailabilityPort.AllocateStockMessage message = allocateStockMapper.toMessage(record);
        InventoryAvailabilityPort.AllocationMessageResult result = availabilityPort.allocateStock(message);
        return allocateStockMapper.toResult(result);
    }

    @Override
    public void deallocateStock(DeallocateStockRecord record) {
        InventoryAvailabilityPort.DeallocateStockMessage message = deallocateStockMapper.toMessage(record);
        availabilityPort.deallocateStock(message);
    }

    @Override
    public List<ReorderSuggestionOutboundResult> listReorderSuggestions(String merchantIdStr, String locationIdStr, String sku) {
        Id merchantId = (merchantIdStr != null && !merchantIdStr.isBlank()) ? idGenerator.convertIdFrom(merchantIdStr) : null;
        Id locationId = (locationIdStr != null && !locationIdStr.isBlank()) ? idGenerator.convertIdFrom(locationIdStr) : null;
        List<InventoryAvailabilityPort.ReorderSuggestionMessageResult> results = availabilityPort.listReorderSuggestions(merchantId, locationId, sku);
        return listReorderSuggestionsMapper.toResultList(results);
    }

    @Override
    public FacilityLocationOutboundResult createLocation(CreateLocationRecord record) {
        InventoryFacilityPort.CreateLocationMessage message = createLocationMapper.toMessage(record);
        InventoryFacilityPort.FacilityLocationResult result = facilityPort.createLocation(message);
        return createLocationMapper.toResult(result);
    }

    @Override
    public Optional<FacilityLocationOutboundResult> findLocationById(String locationIdStr) {
        Id locationId = idGenerator.convertIdFrom(locationIdStr);
        return facilityPort.findLocationById(locationId).map(getLocationMapper::toResult);
    }

    @Override
    public void activateLocation(String locationIdStr) {
        facilityPort.activateLocation(idGenerator.convertIdFrom(locationIdStr));
    }

    @Override
    public void deactivateLocation(String locationIdStr) {
        facilityPort.deactivateLocation(idGenerator.convertIdFrom(locationIdStr));
    }

    @Override
    public void deleteLocation(String locationIdStr) {
        facilityPort.deleteLocation(idGenerator.convertIdFrom(locationIdStr));
    }

    @Override
    public FacilityZoneOutboundResult createZone(CreateZoneRecord record) {
        Id locationId = idGenerator.convertIdFrom(record.locationId());
        InventoryFacilityPort.CreateZoneMessage message = createZoneMapper.toMessage(record);
        InventoryFacilityPort.FacilityZoneResult result = facilityPort.createZone(locationId, message);
        return createZoneMapper.toResult(result);
    }

    @Override
    public Optional<FacilityZoneOutboundResult> findZoneById(String zoneIdStr) {
        Id zoneId = idGenerator.convertIdFrom(zoneIdStr);
        return facilityPort.findZoneById(zoneId).map(getZoneMapper::toResult);
    }

    @Override
    public void activateZone(String zoneIdStr) {
        facilityPort.activateZone(idGenerator.convertIdFrom(zoneIdStr));
    }

    @Override
    public void deactivateZone(String zoneIdStr) {
        facilityPort.deactivateZone(idGenerator.convertIdFrom(zoneIdStr));
    }

    @Override
    public void deleteZone(String zoneIdStr) {
        facilityPort.deleteZone(idGenerator.convertIdFrom(zoneIdStr));
    }

    @Override
    public FacilityBinOutboundResult createBin(CreateBinRecord record) {
        Id zoneId = idGenerator.convertIdFrom(record.zoneId());
        InventoryFacilityPort.CreateBinMessage message = createBinMapper.toMessage(record);
        InventoryFacilityPort.FacilityBinResult result = facilityPort.createBin(zoneId, message);
        return createBinMapper.toResult(result);
    }

    @Override
    public Optional<FacilityBinOutboundResult> findBinById(String binIdStr) {
        Id binId = idGenerator.convertIdFrom(binIdStr);
        return facilityPort.findBinById(binId).map(getBinMapper::toResult);
    }

    @Override
    public void activateBin(String binIdStr) {
        facilityPort.activateBin(idGenerator.convertIdFrom(binIdStr));
    }

    @Override
    public void deactivateBin(String binIdStr) {
        facilityPort.deactivateBin(idGenerator.convertIdFrom(binIdStr));
    }

    @Override
    public void deleteBin(String binIdStr) {
        facilityPort.deleteBin(idGenerator.convertIdFrom(binIdStr));
    }
}
