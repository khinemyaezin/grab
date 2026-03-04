package com.grab.store.inventory.internal.service;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.valueobject.ReorderConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryApplicationService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final IdGenerator idGenerator;

    @Transactional("inventoryTransactionManager")
    public InventoryItem createInventory(
            String sku,
            String productVariantId,
            String locationId,
            int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock,
            String createdBy
    ) {
        Id location = idGenerator.generateId(locationId);
        if (inventoryRepository.existsBySkuAndLocation(sku, location)) {
            throw new IllegalArgumentException("Inventory already exists for sku/location");
        }

        InventoryItem item = InventoryItem.create(
                idGenerator.generateId(),
                sku,
                productVariantId == null || productVariantId.isBlank() ? null : idGenerator.generateId(productVariantId),
                location,
                initialQuantity,
                new ReorderConfig(
                        valueOrZero(safetyStock),
                        valueOrZero(reorderPoint),
                        valueOrZero(reorderQuantity),
                        maxStock
                )
        );
        inventoryRepository.save(item);

        if (initialQuantity > 0) {
            StockMovement movement = StockMovement.create(
                    idGenerator.generateId(),
                    item.getId(),
                    StockMovementType.INITIAL_STOCK,
                    initialQuantity,
                    0,
                    0,
                    0,
                    "INITIAL_STOCK",
                    createdBy == null || createdBy.isBlank() ? null : idGenerator.generateId(createdBy)
            );
            stockMovementRepository.save(movement);
        }

        return item;
    }

    @Transactional("inventoryTransactionManager")
    public InventoryItem receiveStock(String inventoryItemId, int quantity, StockMovementType type, String referenceId, String createdBy) {
        InventoryItem item = getInventoryOrThrow(inventoryItemId);
        StockMovement movement = item.receiveStock(
                quantity,
                type,
                referenceId,
                null,
                createdBy == null || createdBy.isBlank() ? null : idGenerator.generateId(createdBy),
                idGenerator.generateId()
        );
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return item;
    }

    @Transactional("inventoryTransactionManager")
    public InventoryReservation reserveStock(
            String inventoryItemId,
            int quantity,
            String orderId,
            String orderLineId,
            LocalDateTime expiresAt,
            String idempotencyKey,
            String createdBy
    ) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = inventoryReservationRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        InventoryItem item = getInventoryOrThrow(inventoryItemId);
        StockMovement movement = item.reserveStock(
                quantity,
                orderId,
                createdBy == null || createdBy.isBlank() ? null : idGenerator.generateId(createdBy),
                idGenerator.generateId()
        );
        InventoryReservation reservation = InventoryReservation.create(
                idGenerator.generateId(),
                item.getId(),
                orderId,
                orderLineId,
                quantity,
                expiresAt,
                idempotencyKey
        );
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        inventoryReservationRepository.save(reservation);
        return reservation;
    }

    @Transactional("inventoryTransactionManager")
    public InventoryReservation releaseReservation(String inventoryItemId, String reservationId, String createdBy) {
        InventoryItem item = getInventoryOrThrow(inventoryItemId);
        InventoryReservation reservation = inventoryReservationRepository.findById(idGenerator.generateId(reservationId))
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        if (!reservation.isActive()) {
            return reservation;
        }

        StockMovement movement = item.releaseReservation(
                reservation.getQuantity(),
                reservation.getOrderId(),
                createdBy == null || createdBy.isBlank() ? null : idGenerator.generateId(createdBy),
                idGenerator.generateId()
        );
        reservation.release();
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        inventoryReservationRepository.save(reservation);
        return reservation;
    }

    @Transactional("inventoryTransactionManager")
    public InventoryReservation shipReservation(String inventoryItemId, String reservationId, String createdBy) {
        InventoryItem item = getInventoryOrThrow(inventoryItemId);
        InventoryReservation reservation = inventoryReservationRepository.findById(idGenerator.generateId(reservationId))
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        if (!reservation.isActive()) {
            return reservation;
        }

        StockMovement movement = item.shipStock(
                reservation.getQuantity(),
                reservation.getOrderId(),
                createdBy == null || createdBy.isBlank() ? null : idGenerator.generateId(createdBy),
                idGenerator.generateId()
        );
        reservation.fulfill();
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        inventoryReservationRepository.save(reservation);
        return reservation;
    }

    @Transactional("inventoryTransactionManager")
    public InventoryItem adjustStock(String inventoryItemId, int newOnHandQuantity, AdjustmentReason reason, String createdBy) {
        InventoryItem item = getInventoryOrThrow(inventoryItemId);
        StockMovement movement = item.adjustStock(
                newOnHandQuantity,
                reason,
                null,
                createdBy == null || createdBy.isBlank() ? null : idGenerator.generateId(createdBy),
                idGenerator.generateId()
        );
        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return item;
    }

    public InventoryItem getInventory(String inventoryItemId) {
        return getInventoryOrThrow(inventoryItemId);
    }

    public List<StockMovement> getMovements(String inventoryItemId) {
        return stockMovementRepository.findByInventoryItemId(idGenerator.generateId(inventoryItemId));
    }

    public List<InventoryReservation> getReservations(String inventoryItemId) {
        return inventoryReservationRepository.findByInventoryItemId(idGenerator.generateId(inventoryItemId));
    }

    private InventoryItem getInventoryOrThrow(String inventoryItemId) {
        return inventoryRepository.findById(idGenerator.generateId(inventoryItemId))
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryItemId));
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
