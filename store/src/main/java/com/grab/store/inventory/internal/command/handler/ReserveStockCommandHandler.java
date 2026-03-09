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
import com.grab.store.inventory.internal.command.ReserveStockCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReserveStockCommandHandler implements CommandHandler<ReserveStockCommand, InventoryReservationResult> {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public InventoryReservationResult handle(ReserveStockCommand command) {
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            InventoryReservation existing = inventoryReservationRepository.findByIdempotencyKey(command.idempotencyKey())
                    .orElse(null);
            if (existing != null) {
                return mapToInventoryReservationResult(existing);
            }
        }

        InventoryItem item = inventoryRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + command.inventoryItemId().getValue()));

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
        inventoryReservationRepository.save(reservation);
        return mapToInventoryReservationResult(reservation);
    }

    @Override
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
