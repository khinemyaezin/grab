package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.grab.store.inventory.internal.command.ExpireExpiredReservationsCommand;
import com.grab.store.inventory.internal.command.ExpireExpiredReservationsResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireExpiredReservationsCommandHandler
        implements CommandHandler<ExpireExpiredReservationsCommand, ExpireExpiredReservationsResult> {

    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public ExpireExpiredReservationsResult handle(ExpireExpiredReservationsCommand command) {
        List<InventoryReservation> expired = inventoryReservationRepository.findExpiredActive(
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
                inventoryReservationRepository.save(reservation);
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
                inventoryReservationRepository.save(reservation);
                expiredCount++;
            } catch (Exception ex) {
                log.error("Failed to expire reservation {}", reservation.getId().getValue(), ex);
            }
        }

        return new ExpireExpiredReservationsResult(expired.size(), expiredCount);
    }

    @Override
    public Class<ExpireExpiredReservationsCommand> getCommandType() {
        return ExpireExpiredReservationsCommand.class;
    }
}
