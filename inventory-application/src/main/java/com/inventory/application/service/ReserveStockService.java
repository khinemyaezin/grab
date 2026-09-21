package com.inventory.application.service;

import com.inventory.application.port.inbound.ReserveStockUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.InventoryReservationRepository;
import com.inventory.domain.port.outbound.StockMovementRepository;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.model.write.ReserveStockCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReserveStockService implements ReserveStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryReservationRepository inventoryReservationQueryPort;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final IdGenerator idGenerator;

            public InventoryReservationResult execute(ReserveStockCommand command) {
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            InventoryReservation existing = inventoryReservationQueryPort.findByIdempotencyKey(command.idempotencyKey())
                    .orElse(null);
            if (existing != null) {
                return mapToInventoryReservationResult(existing);
            }
        }

        
        InventoryItem item = inventoryRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(command.inventoryItemId().getValue())));

        Location location = locationRepository.findById(item.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(item.getLocationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);
        if (!location.isActive()) {
            throw new InventoryServiceException(new InventoryServiceError.LocationInactive(item.getLocationId().getValue()));
        }

        StockMovement movement = item.reserveStock(
                command.quantity(),
                command.orderId().getValue(),
                command.createdBy(),
                idGenerator.generateId()
        );

        InventoryReservation reservation = InventoryReservation.create(
                idGenerator.generateId(),
                item.getId(),
                command.orderId().getValue(),
                command.orderLineId().getValue(),
                command.quantity(),
                command.expiresAt(),
                command.idempotencyKey()
        );

        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        inventoryReservationQueryPort.save(reservation);
        return mapToInventoryReservationResult(reservation);
    }

        public Class<ReserveStockCommand> getCommandType() {
        return ReserveStockCommand.class;
    }

    private InventoryReservationResult mapToInventoryReservationResult(InventoryReservation reservation) {
        return new InventoryReservationResult(
                reservation.getId().getValue(),
                reservation.getInventoryItemId().getValue(),
                reservation.getOrderId(),
                reservation.getOrderLineId(),
                reservation.getQuantity(),
                reservation.getStatus().name(),
                reservation.getExpiresAt(),
                reservation.getIdempotencyKey()
        );
    }
}
