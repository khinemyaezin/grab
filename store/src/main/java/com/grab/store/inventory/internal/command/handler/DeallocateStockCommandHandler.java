package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.grab.store.inventory.internal.command.DeallocateStockCommand;
import com.grab.store.inventory.internal.command.DeallocateStockResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeallocateStockCommandHandler implements CommandHandler<DeallocateStockCommand, DeallocateStockResult> {

    private final InventoryAllocationService inventoryAllocationService;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public DeallocateStockResult handle(DeallocateStockCommand command) {
        List<InventoryReservation> reservations = inventoryReservationRepository.findActiveByOrderId(command.orderId());
        int released = 0;

        if (!reservations.isEmpty()) {
            for (InventoryReservation reservation : reservations) {
                if (released >= command.quantity()) {
                    break;
                }
                InventoryItem item = inventoryRepository.findById(reservation.getInventoryItemId()).orElse(null);
                if (item == null || !item.getSku().equals(command.sku()) || !reservation.isActive()) {
                    continue;
                }
                int remaining = command.quantity() - released;
                if (remaining <= 0) {
                    break;
                }
                // Release whole reservation rows only to keep reservation status consistent with stock.
                if (reservation.getQuantity() > remaining) {
                    continue;
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
                inventoryReservationRepository.save(reservation);
                released += reservation.getQuantity();
            }
        } else {
            inventoryAllocationService.deallocateStock(
                    command.sku(),
                    command.quantity(),
                    command.orderId(),
                    command.createdBy()
            );
            released = command.quantity();
        }

        return new DeallocateStockResult(command.sku(), command.orderId(), command.quantity(), released);
    }

    @Override
    public Class<DeallocateStockCommand> getCommandType() {
        return DeallocateStockCommand.class;
    }
}
