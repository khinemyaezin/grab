package com.grab.store.inventory.internal.config;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.port.inbound.*;
import com.inventory.application.port.outbound.*;
import com.inventory.application.service.*;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import com.inventory.domain.port.outbound.*;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.ReorderService;
import com.inventory.domain.service.impl.DefaultInventoryAllocationService;
import com.inventory.domain.service.impl.DefaultInventoryStockService;
import com.inventory.domain.service.impl.DefaultReorderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryUseCaseConfig {

    @Bean
    public InventoryAllocationService inventoryAllocationService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            LocationRepository locationRepository,
            ChannelFulfillmentRouteRepository channelFulfillmentRouteRepository,
            IdGenerator idGenerator
    ) {
        return new DefaultInventoryAllocationService(
                inventoryRepository,
                stockMovementRepository,
                locationRepository,
                channelFulfillmentRouteRepository,
                idGenerator
        );
    }

    @Bean
    public ReorderService reorderService(InventoryRepository inventoryRepository) {
        return new DefaultReorderService(inventoryRepository);
    }

    @Bean
    public InventoryStockService inventoryStockService(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            IdGenerator idGenerator
    ) {
        return new DefaultInventoryStockService(
                inventoryRepository,
                stockMovementRepository,
                idGenerator
        );
    }

    @Bean
    public ActivateBinUseCase activateBinUseCase(BinRepository binRepository, ZoneRepository zoneRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new ActivateBinService(binRepository, zoneRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public ActivateInventoryUseCase activateInventoryUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new ActivateInventoryService(inventoryRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public ActivateLocationUseCase activateLocationUseCase(LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new ActivateLocationService(locationRepository, locationAccessPolicy);
    }
    @Bean
    public ActivateZoneUseCase activateZoneUseCase(ZoneRepository zoneRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new ActivateZoneService(zoneRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public AdjustStockUseCase adjustStockUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService) {
        return new AdjustStockService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService);
    }
    @Bean
    public AllocateStockUseCase allocateStockUseCase(InventoryAllocationService inventoryAllocationService, InventoryReservationRepository inventoryReservationQueryPort, ProductVariantViewQueryPort productVariantViewQueryPort, IdGenerator idGenerator) {
        return new AllocateStockService(inventoryAllocationService, inventoryReservationQueryPort, productVariantViewQueryPort, idGenerator);
    }
    @Bean
    public AnnounceInTransitUseCase announceInTransitUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService) {
        return new AnnounceInTransitService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService);
    }
    @Bean
    public CheckChannelStockPathUseCase checkChannelStockPathUseCase(InventoryQueryPort inventoryQueryPort) {
        return new CheckChannelStockPathService(inventoryQueryPort);
    }
    @Bean
    public CheckInventoryExistenceUseCase checkInventoryExistenceUseCase(InventoryQueryPort inventoryQueryPort, IdGenerator idGenerator) {
        return new CheckInventoryExistenceService(inventoryQueryPort, idGenerator);
    }
    @Bean
    public CreateBinUseCase createBinUseCase(ZoneRepository zoneRepository, BinRepository binRepository, IdGenerator idGenerator, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new CreateBinService(zoneRepository, binRepository, idGenerator, locationRepository, locationAccessPolicy);
    }
    @Bean
    public CreateInventoryUseCase createInventoryUseCase(InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository, LocationRepository locationRepository, ProductVariantViewQueryPort productVariantViewQueryPort, IdGenerator idGenerator) {
        return new CreateInventoryService(inventoryRepository, stockMovementRepository, locationRepository, productVariantViewQueryPort, idGenerator);
    }
    @Bean
    public CreateLocationUseCase createLocationUseCase(LocationRepository locationRepository, IdGenerator idGenerator) {
        return new CreateLocationService(locationRepository, idGenerator);
    }
    @Bean
    public CreateZoneUseCase createZoneUseCase(LocationRepository locationRepository, ZoneRepository zoneRepository, IdGenerator idGenerator, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new CreateZoneService(locationRepository, zoneRepository, idGenerator, locationAccessPolicy);
    }
    @Bean
    public DeactivateBinUseCase deactivateBinUseCase(BinRepository binRepository, ZoneRepository zoneRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new DeactivateBinService(binRepository, zoneRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public DeactivateLocationUseCase deactivateLocationUseCase(LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryRepository inventoryRepository) {
        return new DeactivateLocationService(locationRepository, locationAccessPolicy, inventoryRepository);
    }
    @Bean
    public DeactivateZoneUseCase deactivateZoneUseCase(ZoneRepository zoneRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new DeactivateZoneService(zoneRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public DeallocateStockUseCase deallocateStockUseCase(InventoryAllocationService inventoryAllocationService, InventoryReservationRepository inventoryReservationQueryPort, InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository, IdGenerator idGenerator) {
        return new DeallocateStockService(inventoryAllocationService, inventoryReservationQueryPort, inventoryRepository, stockMovementRepository, idGenerator);
    }
    @Bean
    public DeleteBinUseCase deleteBinUseCase(BinRepository binRepository, ZoneRepository zoneRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new DeleteBinService(binRepository, zoneRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public DeleteLocationUseCase deleteLocationUseCase(LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, ZoneRepository zoneRepository) {
        return new DeleteLocationService(locationRepository, locationAccessPolicy, zoneRepository);
    }
    @Bean
    public DeleteZoneUseCase deleteZoneUseCase(ZoneRepository zoneRepository, BinRepository binRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new DeleteZoneService(zoneRepository, binRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public DiscontinueInventoryForDeletedVariantUseCase discontinueInventoryForDeletedVariantUseCase(InventoryRepository inventoryRepository) {
        return new DiscontinueInventoryForDeletedVariantService(inventoryRepository);
    }
    @Bean
    public DiscontinueInventoryUseCase discontinueInventoryUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new DiscontinueInventoryService(inventoryRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public ExpireExpiredReservationsUseCase expireExpiredReservationsUseCase(InventoryReservationRepository inventoryReservationQueryPort, InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository, IdGenerator idGenerator) {
        return new ExpireExpiredReservationsService(inventoryReservationQueryPort, inventoryRepository, stockMovementRepository, idGenerator);
    }
    @Bean
    public GetAllocationAvailabilityUseCase getAllocationAvailabilityUseCase(InventoryQueryPort inventoryQueryPort, ProductVariantViewQueryPort productVariantViewQueryPort) {
        return new GetAllocationAvailabilityService(inventoryQueryPort, productVariantViewQueryPort);
    }
    @Bean
    public GetBinLocationIdUseCase getBinLocationIdUseCase(BinQueryPort binQueryPort, ZoneQueryPort zoneQueryPort) {
        return new GetBinLocationIdService(binQueryPort, zoneQueryPort);
    }
    @Bean
    public GetBinUseCase getBinUseCase(BinQueryPort binQueryPort, IdGenerator idGenerator) {
        return new GetBinService(binQueryPort, idGenerator);
    }
    @Bean
    public GetInventoryLocationIdUseCase getInventoryLocationIdUseCase(InventoryQueryPort inventoryQueryPort) {
        return new GetInventoryLocationIdService(inventoryQueryPort);
    }
    @Bean
    public GetInventoryMovementsUseCase getInventoryMovementsUseCase(StockMovementQueryPort stockMovementRepository, IdGenerator idGenerator) {
        return new GetInventoryMovementsService(stockMovementRepository, idGenerator);
    }
    @Bean
    public GetInventoryReservationUseCase getInventoryReservationUseCase(InventoryReservationQueryPort inventoryReservationQueryPort, IdGenerator idGenerator) {
        return new GetInventoryReservationService(inventoryReservationQueryPort, idGenerator);
    }
    @Bean
    public GetInventoryUseCase getInventoryUseCase(InventoryQueryPort inventoryQueryPort, ProductVariantViewQueryPort productVariantViewQueryPort, IdGenerator idGenerator) {
        return new GetInventoryService(inventoryQueryPort, productVariantViewQueryPort, idGenerator);
    }
    @Bean
    public GetInventorySummaryUseCase getInventorySummaryUseCase(InventoryQueryPort inventoryQueryPort) {
        return new GetInventorySummaryService(inventoryQueryPort);
    }
    @Bean
    public GetLocationByCodeUseCase getLocationByCodeUseCase(LocationQueryPort locationQueryPort, IdGenerator idGenerator) {
        return new GetLocationByCodeService(locationQueryPort, idGenerator);
    }
    @Bean
    public GetLocationUseCase getLocationUseCase(LocationQueryPort locationQueryPort, IdGenerator idGenerator) {
        return new GetLocationService(locationQueryPort, idGenerator);
    }
    @Bean
    public GetReorderSuggestionsUseCase getReorderSuggestionsUseCase(InventoryQueryPort inventoryQueryPort, ProductVariantViewQueryPort productVariantViewQueryPort) {
        return new GetReorderSuggestionsService(inventoryQueryPort, productVariantViewQueryPort);
    }
    @Bean
    public GetZoneLocationIdUseCase getZoneLocationIdUseCase(ZoneQueryPort zoneQueryPort) {
        return new GetZoneLocationIdService(zoneQueryPort);
    }
    @Bean
    public GetZoneUseCase getZoneUseCase(ZoneQueryPort zoneQueryPort, IdGenerator idGenerator) {
        return new GetZoneService(zoneQueryPort, idGenerator);
    }
    @Bean
    public ListBinsByZoneUseCase listBinsByZoneUseCase(BinQueryPort binRepository, IdGenerator idGenerator) {
        return new ListBinsByZoneService(binRepository, idGenerator);
    }
    @Bean
    public ListLocationsUseCase listLocationsUseCase(LocationQueryPort locationRepository, IdGenerator idGenerator) {
        return new ListLocationsService(locationRepository, idGenerator);
    }
    @Bean
    public ListZonesByLocationUseCase listZonesByLocationUseCase(ZoneQueryPort zoneRepository, IdGenerator idGenerator) {
        return new ListZonesByLocationService(zoneRepository, idGenerator);
    }
    @Bean
    public MarkDamagedUseCase markDamagedUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService) {
        return new MarkDamagedService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService);
    }
    @Bean
    public ReceiveInTransitUseCase receiveInTransitUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService) {
        return new ReceiveInTransitService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService);
    }
    @Bean
    public ReceiveStockUseCase receiveStockUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService) {
        return new ReceiveStockService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService);
    }
    @Bean
    public ReleaseReservationUseCase releaseReservationUseCase(InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository, InventoryReservationRepository inventoryReservationQueryPort, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, IdGenerator idGenerator) {
        return new ReleaseReservationService(inventoryRepository, stockMovementRepository, inventoryReservationQueryPort, locationRepository, locationAccessPolicy, idGenerator);
    }
    @Bean
    public ReserveStockUseCase reserveStockUseCase(InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository, InventoryReservationRepository inventoryReservationQueryPort, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, IdGenerator idGenerator) {
        return new ReserveStockService(inventoryRepository, stockMovementRepository, inventoryReservationQueryPort, locationRepository, locationAccessPolicy, idGenerator);
    }
    @Bean
    public ReturnToVendorUseCase returnToVendorUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService) {
        return new ReturnToVendorService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService);
    }
    @Bean
    public SearchBinsUseCase searchBinsUseCase(BinQueryPort binQueryPort, IdGenerator idGenerator) {
        return new SearchBinsService(binQueryPort, idGenerator);
    }
    @Bean
    public SearchInventoryUseCase searchInventoryUseCase(InventoryQueryPort inventoryQueryPort, ProductVariantViewQueryPort productVariantViewQueryPort, IdGenerator idGenerator) {
        return new SearchInventoryService(inventoryQueryPort, productVariantViewQueryPort, idGenerator);
    }
    @Bean
    public SearchLocationsUseCase searchLocationsUseCase(LocationQueryPort locationQueryPort, IdGenerator idGenerator) {
        return new SearchLocationsService(locationQueryPort, idGenerator);
    }
    @Bean
    public SearchZonesUseCase searchZonesUseCase(ZoneQueryPort zoneQueryPort, IdGenerator idGenerator) {
        return new SearchZonesService(zoneQueryPort, idGenerator);
    }
    @Bean
    public ShipReservationUseCase shipReservationUseCase(InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository, InventoryReservationRepository inventoryReservationQueryPort, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, IdGenerator idGenerator) {
        return new ShipReservationService(inventoryRepository, stockMovementRepository, inventoryReservationQueryPort, locationRepository, locationAccessPolicy, idGenerator);
    }
    @Bean
    public SuspendInventoryUseCase suspendInventoryUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new SuspendInventoryService(inventoryRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public TransferInventoryUseCase transferInventoryUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService, IdGenerator idGenerator) {
        return new TransferInventoryService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService, idGenerator);
    }
    @Bean
    public UpdateBinUseCase updateBinUseCase(BinRepository binRepository, ZoneRepository zoneRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new UpdateBinService(binRepository, zoneRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public UpdateLocationUseCase updateLocationUseCase(LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new UpdateLocationService(locationRepository, locationAccessPolicy);
    }
    @Bean
    public UpdateReorderConfigUseCase updateReorderConfigUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new UpdateReorderConfigService(inventoryRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public UpdateZoneUseCase updateZoneUseCase(ZoneRepository zoneRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy) {
        return new UpdateZoneService(zoneRepository, locationRepository, locationAccessPolicy);
    }
    @Bean
    public WriteOffStockUseCase writeOffStockUseCase(InventoryRepository inventoryRepository, LocationRepository locationRepository, InventoryLocationAccessPolicy locationAccessPolicy, InventoryStockService inventoryStockService) {
        return new WriteOffStockService(inventoryRepository, locationRepository, locationAccessPolicy, inventoryStockService);
    }
}
