package com.inventory.application.service;

import com.inventory.application.port.inbound.ReleaseReservationUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.InventoryReservationRepository;
import com.inventory.domain.port.outbound.StockMovementRepository;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.model.write.ReleaseReservationCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReleaseReservationService implements ReleaseReservationUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryReservationRepository inventoryReservationQueryPort;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final IdGenerator idGenerator;

            public InventoryReservationResult execute(ReleaseReservationCommand command) {
        
        InventoryItem item = inventoryRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(command.inventoryItemId().getValue())));

        Location location = locationRepository.findById(item.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(item.getLocationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);
InventoryReservation reservation = inventoryReservationQueryPort.findById(command.reservationId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.ReservationNotFound(command.reservationId().getValue())));

        if (!item.getId().equals(reservation.getInventoryItemId())) {
            throw new InventoryServiceException(new InventoryServiceError.ReservationInventoryMismatch(command.reservationId().getValue(), command.inventoryItemId().getValue()));
        }

        if (!reservation.isActive()) {
            return mapToInventoryReservationResult(reservation);
        }

        StockMovement movement = item.releaseReservation(
                reservation.getQuantity(),
                reservation.getOrderId(),
                command.createdBy(),
                idGenerator.generateId()
        );
        reservation.release();

        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        inventoryReservationQueryPort.save(reservation);

        return mapToInventoryReservationResult(reservation);
    }

        public Class<ReleaseReservationCommand> getCommandType() {
        return ReleaseReservationCommand.class;
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
