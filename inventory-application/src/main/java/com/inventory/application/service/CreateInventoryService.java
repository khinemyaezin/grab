package com.inventory.application.service;

import com.inventory.application.port.inbound.CreateInventoryUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.StockMovementRepository;
import com.inventory.domain.valueobject.ReorderConfig;
import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.model.read.ProductView;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateInventoryService implements CreateInventoryUseCase {

    private static final Logger log = Loggers.getLogger(CreateInventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LocationRepository locationRepository;
    private final ProductVariantViewQueryPort productVariantViewQueryPort;
    private final IdGenerator idGenerator;

            public InventoryItemResult execute(CreateInventoryCommand command) {
        log.info("Creating inventory for sku={} at locationId={}", command.sku(), command.locationId().getValue());
        
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> {
                    log.warn("Location not found: locationId={}", command.locationId().getValue());
                    return new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue()));
                });
        if (!location.isActive()) {
            log.warn("Location is inactive: locationId={}", command.locationId().getValue());
            throw new InventoryServiceException(new InventoryServiceError.LocationInactive(command.locationId().getValue()));
        }

        Id productVariantId = resolveProductVariantId(command);

        if (inventoryRepository.existsBySkuAndLocation(command.sku(), command.locationId())) {
            log.warn("Inventory already exists for sku={} at locationId={}", command.sku(), command.locationId().getValue());
            throw new InventoryServiceException(new InventoryServiceError.InventoryAlreadyExistsForSkuLocation(command.sku(), command.locationId().getValue()));
        }

        int safetyStock = valueOrZero(command.safetyStock());
        int recorderPoint = valueOrZero(command.reorderPoint());
        recorderPoint = recorderPoint == 0 && safetyStock > 0 ? safetyStock : recorderPoint;
        int recorderQuantity = valueOrZero(command.reorderQuantity());

        InventoryItem item = InventoryItem.create(
                idGenerator.generateId(),
                command.sku(),
                command.merchantId(),
                productVariantId,
                command.locationId(),
                command.initialQuantity(),
                new ReorderConfig(
                        safetyStock,
                        recorderPoint,
                        recorderQuantity,
                        command.maxStock()
                )
        );
        inventoryRepository.save(item);

        if (command.initialQuantity() > 0) {
            log.info("Creating initial stock movement for inventoryItemUuid={}, quantity={}", item.getId().getValue(), command.initialQuantity());
            StockMovement movement = StockMovement.create(
                    idGenerator.generateId(),
                    item.getId(),
                    StockMovementType.INITIAL_STOCK,
                    command.initialQuantity(),
                    0,
                    0,
                    0,
                    "INITIAL_STOCK",
                    command.createdBy()
            );
            stockMovementRepository.save(movement);
        }

        log.info("Created inventory with id={}, sku={}, locationId={}", item.getId().getValue(), item.getSku(), item.getLocationId().getValue());

        return mapToInventoryItemResult(item);
    }

        public Class<CreateInventoryCommand> getCommandType() {
        return CreateInventoryCommand.class;
    }

    private Id resolveProductVariantId(CreateInventoryCommand command) {
        if (command.variantId() != null && !command.variantId().isBlank()) {
            return idGenerator.convertIdFrom(command.variantId());
        }
        ProductView variantView = resolveActiveProductVariantBySku(command.sku());
        if (!variantView.isManageInventory()) {
            log.warn("Inventory is not managed for sku={}", command.sku());
            throw new InventoryServiceException(new InventoryServiceError.InventoryNotManaged(command.sku()));
        }
        return idGenerator.convertIdFrom(variantView.getVariantUuid());
    }

    private ProductView resolveActiveProductVariantBySku(String sku) {
        return productVariantViewQueryPort.findActiveBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Active product variant not found in projection for sku={}", sku);
                    return new InventoryServiceException(new InventoryServiceError.ProductVariantNotFound(sku));
                });
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private InventoryItemResult mapToInventoryItemResult(InventoryItem item) {
        return InventoryItemResults.from(item);
    }
}
