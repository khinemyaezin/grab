package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.grab.store.inventory.internal.command.InventoryItemResults;
import com.grab.store.inventory.internal.command.TransferInventoryCommand;
import com.grab.store.inventory.internal.command.TransferInventoryResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferInventoryCommandHandler implements CommandHandler<TransferInventoryCommand, TransferInventoryResult> {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public TransferInventoryResult handle(TransferInventoryCommand command) {
        InventoryItem source = inventoryRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(command.inventoryItemId().getValue())));

        Location fromLocation = locationRepository.findById(source.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(source.getLocationId().getValue())));
        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), fromLocation);
        if (!fromLocation.isActive()) {
            throw new InventoryServiceException(new InventoryServiceError.LocationInactive(fromLocation.getId().getValue()));
        }

        Location toLocation = locationRepository.findById(command.toLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(command.toLocationId().getValue())));
        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), toLocation);
        if (!toLocation.isActive()) {
            throw new InventoryServiceException(new InventoryServiceError.LocationInactive(toLocation.getId().getValue()));
        }

        if (source.getLocationId().getValue().equals(toLocation.getId().getValue())) {
            throw new InventoryServiceException(new InventoryServiceError.TransferSameLocation(
                    source.getLocationId().getValue()));
        }

        String transferId = idGenerator.generateId().getValue();

        StockMovementResult outResult = inventoryStockService.transferOut(
                source.getId(), command.quantity(), transferId, command.createdBy());

        InventoryItem destination = inventoryRepository.findBySkuAndLocation(source.getSku(), toLocation.getId())
                .orElseGet(() -> createDestinationItem(source, toLocation.getId()));

        StockMovementResult inResult = inventoryStockService.receiveStock(
                destination.getId(),
                command.quantity(),
                StockMovementType.TRANSFER_IN,
                transferId,
                command.notes(),
                command.createdBy()
        );

        return new TransferInventoryResult(
                InventoryItemResults.from(outResult.item()),
                InventoryItemResults.from(inResult.item()),
                transferId
        );
    }

    @Override
    public Class<TransferInventoryCommand> getCommandType() {
        return TransferInventoryCommand.class;
    }

    private InventoryItem createDestinationItem(InventoryItem source, Id toLocationId) {
        InventoryItem created = InventoryItem.create(
                idGenerator.generateId(),
                source.getSku(),
                source.getMerchantId(),
                source.getProductVariantId(),
                toLocationId,
                0,
                source.getReorderConfig()
        );
        inventoryRepository.save(created);
        return created;
    }
}
