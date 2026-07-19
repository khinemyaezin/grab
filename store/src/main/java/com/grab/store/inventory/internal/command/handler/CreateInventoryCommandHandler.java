package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.valueobject.ReorderConfig;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.InventoryItemResults;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.infrastructure.view.ProductView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateInventoryCommandHandler implements CommandHandler<CreateInventoryCommand, InventoryItemResult> {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LocationRepository locationRepository;
    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(CreateInventoryCommand command) {
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

        ProductView variantView = resolveActiveProductVariantBySku(command.sku());

        if (inventoryRepository.existsBySkuAndLocation(command.sku(), command.locationId())) {
            log.warn("Inventory already exists for sku={} at locationId={}", command.sku(), command.locationId().getValue());
            throw new InventoryServiceException(new InventoryServiceError.InventoryAlreadyExistsForSkuLocation(command.sku(), command.locationId().getValue()));
        }

        InventoryItem item = InventoryItem.create(
                idGenerator.generateId(),
                command.sku(),
                command.merchantId(),
                idGenerator.convertIdFrom(variantView.getVariantUuid()),
                command.locationId(),
                command.initialQuantity(),
                new ReorderConfig(
                        valueOrZero(command.safetyStock()),
                        valueOrZero(command.reorderPoint()),
                        valueOrZero(command.reorderQuantity()),
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

    @Override
    public Class<CreateInventoryCommand> getCommandType() {
        return CreateInventoryCommand.class;
    }

    private ProductView resolveActiveProductVariantBySku(String sku) {
        return productVariantViewJpaRepository.findBySkuAndStatus(sku, ProductVariantViewEntity.STATUS_ACTIVE)
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
