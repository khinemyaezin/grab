package com.inventory.application.service;

import com.inventory.application.port.inbound.ExpireExpiredReservationsUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.InventoryReservationRepository;
import com.inventory.domain.port.outbound.StockMovementRepository;
import com.inventory.application.model.write.ExpireExpiredReservationsCommand;
import com.inventory.application.model.write.ExpireExpiredReservationsResult;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ExpireExpiredReservationsService implements ExpireExpiredReservationsUseCase {

    private static final Logger log = Loggers.getLogger(ExpireExpiredReservationsService.class);

    private final InventoryReservationRepository inventoryReservationQueryPort;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final IdGenerator idGenerator;

            public ExpireExpiredReservationsResult execute(ExpireExpiredReservationsCommand command) {
        List<InventoryReservation> expired = inventoryReservationQueryPort.findExpiredActive(
                command.asOf(),
                command.batchSize()
        );

        int expiredCount = 0;
        for (InventoryReservation reservation : expired) {
            if (!reservation.isActive()) {
                continue;
            }
            InventoryItem item = inventoryRepository.findById(reservation.getInventoryItemId()).orElse(null);
            if (item == null) {
                reservation.expire();
                inventoryReservationQueryPort.save(reservation);
                expiredCount++;
                log.warn("Expired reservation {} with missing inventory item {}",
                        reservation.getId().getValue(), reservation.getInventoryItemId().getValue());
                continue;
            }
            try {
                StockMovement movement = item.releaseReservation(
                        reservation.getQuantity(),
                        reservation.getOrderId(),
                        command.createdBy(),
                        idGenerator.generateId()
                );
                reservation.expire();
                inventoryRepository.save(item);
                stockMovementRepository.save(movement);
                inventoryReservationQueryPort.save(reservation);
                expiredCount++;
            } catch (Exception ex) {
                log.error("Failed to expire reservation {}", reservation.getId().getValue(), ex);
            }
        }

        return new ExpireExpiredReservationsResult(expired.size(), expiredCount);
    }

        public Class<ExpireExpiredReservationsCommand> getCommandType() {
        return ExpireExpiredReservationsCommand.class;
    }
}
