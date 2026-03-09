package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.ShipReservationCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipReservationCommandHandler implements CommandHandler<ShipReservationCommand, InventoryReservationResult> {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public InventoryReservationResult handle(ShipReservationCommand command) {
        InventoryItem item = inventoryRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + command.inventoryItemId().getValue()));

        InventoryReservation reservation = inventoryReservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + command.reservationId().getValue()));

        if (!item.getId().equals(reservation.getInventoryItemId())) {
            throw new IllegalArgumentException("Reservation does not belong to inventory item: " + command.inventoryItemId().getValue());
        }

        if (!reservation.isActive()) {
            return mapToInventoryReservationResult(reservation);
        }

        StockMovement movement = item.shipStock(
                reservation.getQuantity(),
                reservation.getOrderId(),
                command.createdBy(),
                idGenerator.generateId()
        );
        reservation.fulfill();

        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        inventoryReservationRepository.save(reservation);

        return mapToInventoryReservationResult(reservation);
    }

    @Override
    public Class<ShipReservationCommand> getCommandType() {
        return ShipReservationCommand.class;
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
