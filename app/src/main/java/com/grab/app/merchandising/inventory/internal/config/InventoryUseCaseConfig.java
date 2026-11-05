package com.grab.app.merchandising.inventory.internal.config;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.policy.ProductVariantProjectionPolicy;
import com.inventory.application.port.inbound.ActivateBinUseCase;
import com.inventory.application.port.inbound.ActivateInventoryUseCase;
import com.inventory.application.port.inbound.ActivateLocationUseCase;
import com.inventory.application.port.inbound.ActivateZoneUseCase;
import com.inventory.application.port.inbound.AdjustStockUseCase;
import com.inventory.application.port.inbound.AllocateStockUseCase;
import com.inventory.application.port.inbound.AnnounceInTransitUseCase;
import com.inventory.application.port.inbound.CheckChannelStockPathUseCase;
import com.inventory.application.port.inbound.CheckInventoryExistenceUseCase;
import com.inventory.application.port.inbound.CreateBinUseCase;
import com.inventory.application.port.inbound.CreateInventoryUseCase;
import com.inventory.application.port.inbound.CreateLocationUseCase;
import com.inventory.application.port.inbound.CreateZoneUseCase;
import com.inventory.application.port.inbound.DeactivateBinUseCase;
import com.inventory.application.port.inbound.DeactivateLocationUseCase;
import com.inventory.application.port.inbound.DeactivateZoneUseCase;
import com.inventory.application.port.inbound.DeallocateStockUseCase;
import com.inventory.application.port.inbound.DeleteBinUseCase;
import com.inventory.application.port.inbound.DeleteLocationUseCase;
import com.inventory.application.port.inbound.DeleteZoneUseCase;
import com.inventory.application.port.inbound.DiscontinueInventoryForDeletedVariantUseCase;
import com.inventory.application.port.inbound.DiscontinueInventoryUseCase;
import com.inventory.application.port.inbound.ExpireExpiredReservationsUseCase;
import com.inventory.application.port.inbound.GetAllocationAvailabilityUseCase;
import com.inventory.application.port.inbound.GetBinLocationIdUseCase;
import com.inventory.application.port.inbound.GetBinUseCase;
import com.inventory.application.port.inbound.GetDiscoveryInventoryFactsUseCase;
import com.inventory.application.port.inbound.GetInventoryLocationIdUseCase;
import com.inventory.application.port.inbound.GetInventoryMovementsUseCase;
import com.inventory.application.port.inbound.GetInventoryReservationUseCase;
import com.inventory.application.port.inbound.GetInventorySummaryUseCase;
import com.inventory.application.port.inbound.GetInventoryUseCase;
import com.inventory.application.port.inbound.GetLocationByCodeUseCase;
import com.inventory.application.port.inbound.GetLocationUseCase;
import com.inventory.application.port.inbound.GetReorderSuggestionsUseCase;
import com.inventory.application.port.inbound.GetZoneLocationIdUseCase;
import com.inventory.application.port.inbound.GetZoneUseCase;
import com.inventory.application.port.inbound.ListBinsByZoneUseCase;
import com.inventory.application.port.inbound.ListLocationsUseCase;
import com.inventory.application.port.inbound.ListSkusAtLocationUseCase;
import com.inventory.application.port.inbound.ListZonesByLocationUseCase;
import com.inventory.application.port.inbound.MarkDamagedUseCase;
import com.inventory.application.port.inbound.ProjectProductVariantViewUseCase;
import com.inventory.application.port.inbound.ReceiveInTransitUseCase;
import com.inventory.application.port.inbound.ReceiveStockUseCase;
import com.inventory.application.port.inbound.ReleaseReservationUseCase;
import com.inventory.application.port.inbound.ReserveStockUseCase;
import com.inventory.application.port.inbound.ReturnToVendorUseCase;
import com.inventory.application.port.inbound.SearchBinsUseCase;
import com.inventory.application.port.inbound.SearchInventoryUseCase;
import com.inventory.application.port.inbound.SearchLocationsUseCase;
import com.inventory.application.port.inbound.SearchZonesUseCase;
import com.inventory.application.port.inbound.ShipReservationUseCase;
import com.inventory.application.port.inbound.SuspendInventoryUseCase;
import com.inventory.application.port.inbound.TransferInventoryUseCase;
import com.inventory.application.port.inbound.UpdateBinUseCase;
import com.inventory.application.port.inbound.UpdateLocationUseCase;
import com.inventory.application.port.inbound.UpdateReorderConfigUseCase;
import com.inventory.application.port.inbound.UpdateZoneUseCase;
import com.inventory.application.port.inbound.WriteOffStockUseCase;
import com.inventory.application.port.outbound.BinQueryPort;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.InventoryReservationQueryPort;
import com.inventory.application.port.outbound.LocationQueryPort;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.port.outbound.StockMovementQueryPort;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.application.service.ActivateBinService;
import com.inventory.application.service.ActivateInventoryService;
import com.inventory.application.service.ActivateLocationService;
import com.inventory.application.service.ActivateZoneService;
import com.inventory.application.service.AdjustStockService;
import com.inventory.application.service.AllocateStockService;
import com.inventory.application.service.AnnounceInTransitService;
import com.inventory.application.service.CheckChannelStockPathService;
import com.inventory.application.service.CheckInventoryExistenceService;
import com.inventory.application.service.CreateBinService;
import com.inventory.application.service.CreateInventoryService;
import com.inventory.application.service.CreateLocationService;
import com.inventory.application.service.CreateZoneService;
import com.inventory.application.service.DeactivateBinService;
import com.inventory.application.service.DeactivateLocationService;
import com.inventory.application.service.DeactivateZoneService;
import com.inventory.application.service.DeallocateStockService;
import com.inventory.application.service.DeleteBinService;
import com.inventory.application.service.DeleteLocationService;
import com.inventory.application.service.DeleteZoneService;
import com.inventory.application.service.DiscontinueInventoryForDeletedVariantService;
import com.inventory.application.service.DiscontinueInventoryService;
import com.inventory.application.service.ExpireExpiredReservationsService;
import com.inventory.application.service.GetAllocationAvailabilityService;
import com.inventory.application.service.GetBinLocationIdService;
import com.inventory.application.service.GetBinService;
import com.inventory.application.service.GetDiscoveryInventoryFactsService;
import com.inventory.application.service.GetInventoryLocationIdService;
import com.inventory.application.service.GetInventoryMovementsService;
import com.inventory.application.service.GetInventoryReservationService;
import com.inventory.application.service.GetInventoryService;
import com.inventory.application.service.GetInventorySummaryService;
import com.inventory.application.service.GetLocationByCodeService;
import com.inventory.application.service.GetLocationService;
import com.inventory.application.service.GetReorderSuggestionsService;
import com.inventory.application.service.GetZoneLocationIdService;
import com.inventory.application.service.GetZoneService;
import com.inventory.application.service.ListBinsByZoneService;
import com.inventory.application.service.ListLocationsService;
import com.inventory.application.service.ListSkusAtLocationService;
import com.inventory.application.service.ListZonesByLocationService;
import com.inventory.application.service.MarkDamagedService;
import com.inventory.application.service.ProjectProductVariantViewService;
import com.inventory.application.service.ReceiveInTransitService;
import com.inventory.application.service.ReceiveStockService;
import com.inventory.application.service.ReleaseReservationService;
import com.inventory.application.service.ReserveStockService;
import com.inventory.application.service.ReturnToVendorService;
import com.inventory.application.service.SearchBinsService;
import com.inventory.application.service.SearchInventoryService;
import com.inventory.application.service.SearchLocationsService;
import com.inventory.application.service.SearchZonesService;
import com.inventory.application.service.ShipReservationService;
import com.inventory.application.service.SuspendInventoryService;
import com.inventory.application.service.TransferInventoryService;
import com.inventory.application.service.UpdateBinService;
import com.inventory.application.service.UpdateLocationService;
import com.inventory.application.service.UpdateReorderConfigService;
import com.inventory.application.service.UpdateZoneService;
import com.inventory.application.service.WriteOffStockService;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import com.inventory.domain.port.outbound.BinRepository;
import com.inventory.domain.port.outbound.ChannelFulfillmentRouteRepository;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.InventoryReservationRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.application.port.outbound.ProductVariantProjectionRepository;
import com.inventory.domain.port.outbound.StockMovementRepository;
import com.inventory.domain.port.outbound.ZoneRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.ReorderService;
import com.inventory.domain.service.impl.DefaultInventoryAllocationService;
import com.inventory.domain.service.impl.DefaultInventoryStockService;
import com.inventory.domain.service.impl.DefaultReorderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class InventoryUseCaseConfig {

    @Bean
    public ProjectProductVariantViewUseCase projectProductVariantViewUseCase(ProductVariantProjectionRepository repository) {
        ProductVariantProjectionPolicy policy = new ProductVariantProjectionPolicy();
        return new ProjectProductVariantViewService(repository, policy);
    }

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
    public GetDiscoveryInventoryFactsUseCase getDiscoveryInventoryFactsUseCase(InventoryQueryPort inventoryQueryPort) {
        return new GetDiscoveryInventoryFactsService(inventoryQueryPort);
    }

    @Bean
    public ListSkusAtLocationUseCase listSkusAtLocationUseCase(InventoryQueryPort inventoryQueryPort) {
        return new ListSkusAtLocationService(inventoryQueryPort);
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
