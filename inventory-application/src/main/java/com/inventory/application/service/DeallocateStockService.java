package com.inventory.application.service;

import com.inventory.application.port.inbound.DeallocateStockUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.InventoryReservationRepository;
import com.inventory.domain.port.outbound.StockMovementRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.application.model.write.DeallocateStockCommand;
import com.inventory.application.model.write.DeallocateStockResult;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DeallocateStockService implements DeallocateStockUseCase {

    private final InventoryAllocationService inventoryAllocationService;
    private final InventoryReservationRepository inventoryReservationQueryPort;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final IdGenerator idGenerator;

            public DeallocateStockResult execute(DeallocateStockCommand command) {
        List<InventoryReservation> reservations = inventoryReservationQueryPort.findActiveByOrderId(command.orderId());
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
                inventoryReservationQueryPort.save(reservation);
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

        public Class<DeallocateStockCommand> getCommandType() {
        return DeallocateStockCommand.class;
    }
}
