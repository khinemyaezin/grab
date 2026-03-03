package com.inventory.domain.entity;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.StockMovementType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class StockMovement extends Entity<Id> {

    private final Id inventoryItemId;
    private final StockMovementType type;
    private final int quantity;
    private final int quantityBefore;
    private final int quantityAfter;
    private final String referenceId;
    private final LocalDateTime createdAt;
    private final Id createdBy;

    public StockMovement(
            Id id,
            Id inventoryItemId,
            StockMovementType type,
            int quantity,
            int quantityBefore,
            int quantityAfter,
            String referenceId,
            LocalDateTime createdAt,
            Id createdBy
    ) {
        super(id);
        this.inventoryItemId = Objects.requireNonNull(inventoryItemId, "inventoryItemId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.quantity = quantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.referenceId = referenceId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.createdBy = createdBy;
    }

    public static StockMovement create(
            Id id,
            Id inventoryItemId,
            StockMovementType type,
            int quantity,
            int quantityBefore,
            String referenceId,
            Id createdBy
    ) {
        int quantityAfter = calculateQuantityAfter(type, quantityBefore, quantity);
        return new StockMovement(
                id, inventoryItemId, type, quantity, quantityBefore, quantityAfter,
                referenceId, LocalDateTime.now(), createdBy
        );
    }

    private static int calculateQuantityAfter(StockMovementType type, int before, int quantity) {
        return switch (type) {
            case PURCHASE_ORDER_RECEIPT, CUSTOMER_RETURN, TRANSFER_IN, INITIAL_STOCK, RESERVATION_RELEASE ->
                    before + quantity;
            case SALE, TRANSFER_OUT, RETURN_TO_VENDOR, WRITE_OFF, RESERVATION ->
                    before - quantity;
            case CYCLE_COUNT_ADJUSTMENT, DAMAGE_ADJUSTMENT, SHRINKAGE ->
                    before + quantity;
        };
    }

    public boolean isInbound() {
        return type == StockMovementType.PURCHASE_ORDER_RECEIPT
                || type == StockMovementType.CUSTOMER_RETURN
                || type == StockMovementType.TRANSFER_IN
                || type == StockMovementType.INITIAL_STOCK;
    }

    public boolean isOutbound() {
        return type == StockMovementType.SALE
                || type == StockMovementType.TRANSFER_OUT
                || type == StockMovementType.RETURN_TO_VENDOR
                || type == StockMovementType.WRITE_OFF;
    }

    public boolean isAdjustment() {
        return type == StockMovementType.CYCLE_COUNT_ADJUSTMENT
                || type == StockMovementType.DAMAGE_ADJUSTMENT
                || type == StockMovementType.SHRINKAGE;
    }

    public boolean isReservation() {
        return type == StockMovementType.RESERVATION
                || type == StockMovementType.RESERVATION_RELEASE;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "id=" + getId().getValue() +
                ", type=" + type +
                ", quantity=" + quantity +
                ", before=" + quantityBefore +
                ", after=" + quantityAfter +
                ", reference=" + referenceId +
                '}';
    }
}
