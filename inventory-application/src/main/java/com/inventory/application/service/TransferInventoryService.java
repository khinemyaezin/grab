package com.inventory.application.service;

import com.inventory.application.port.inbound.TransferInventoryUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.model.write.TransferInventoryCommand;
import com.inventory.application.model.write.TransferInventoryResult;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransferInventoryService implements TransferInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;
    private final IdGenerator idGenerator;

            public TransferInventoryResult execute(TransferInventoryCommand command) {
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
