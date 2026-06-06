package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.valueobject.ReorderConfig;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateInventoryCommandHandler implements CommandHandler<CreateInventoryCommand, InventoryItemResult> {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LocationRepository locationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(CreateInventoryCommand command) {
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(command.locationId().getValue())));
        if (!location.isActive()) {
            throw new InventoryServiceException(new InventoryServiceError.LocationInactive(command.locationId().getValue()));
        }

        if (inventoryRepository.existsBySkuAndLocation(command.sku(), command.locationId())) {
            throw new InventoryServiceException(new InventoryServiceError.InventoryAlreadyExistsForSkuLocation(command.sku(), command.locationId().getValue()));
        }

        InventoryItem item = InventoryItem.create(
                idGenerator.generateId(),
                command.sku(),
                command.sellerId(),
                command.productVariantId(),
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

        return mapToInventoryItemResult(item);
    }

    @Override
    public Class<CreateInventoryCommand> getCommandType() {
        return CreateInventoryCommand.class;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private InventoryItemResult mapToInventoryItemResult(InventoryItem item) {
        return new InventoryItemResult(
                item.getId().getValue(),
                item.getSku(),
                item.getSellerId().getValue(),
                item.getProductVariantId() == null ? null : item.getProductVariantId().getValue(),
                item.getLocationId().getValue(),
                item.getQuantity().onHand(),
                item.getQuantity().reserved(),
                item.getQuantity().damaged(),
                item.getAvailableQuantity(),
                item.getStatus().name(),
                item.getReorderConfig().safetyStock(),
                item.getReorderConfig().reorderPoint(),
                item.getReorderConfig().reorderQuantity(),
                item.getReorderConfig().maxStock()
        );
    }
}
