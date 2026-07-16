package com.inventory.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.event.InventoryItemDiscontinuedEvent;
import com.inventory.domain.event.LowStockAlertEvent;
import com.inventory.domain.event.StockAdjustedEvent;
import com.inventory.domain.event.StockReceivedEvent;
import com.inventory.domain.event.StockReservedEvent;
import com.inventory.domain.event.StockShippedEvent;
import com.inventory.domain.exception.InventoryDomainError;
import com.inventory.domain.exception.InventoryDomainValidationException;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class InventoryItem extends AggregateRoot<Id> {

    private final String sku;
    private final Id merchantId;
    private final Id productVariantId;
    private final Id locationId;
    private final ReorderConfig reorderConfig;
    private InventoryQuantity quantity;
    private InventoryStatus status;
    private LocalDateTime lastUpdated;

    public InventoryItem(
            Id id,
            String sku,
            Id merchantId,
            Id productVariantId,
            Id locationId,
            InventoryQuantity quantity,
            ReorderConfig reorderConfig,
            InventoryStatus status,
            LocalDateTime lastUpdated
    ) {
        super(id);
        this.sku = Objects.requireNonNull(sku, "sku is required");
        this.merchantId = Objects.requireNonNull(merchantId, "merchantId is required");
        this.productVariantId = productVariantId;
        this.locationId = Objects.requireNonNull(locationId, "locationId is required");
        this.quantity = quantity != null ? quantity : InventoryQuantity.zero();
        this.reorderConfig = reorderConfig != null ? reorderConfig : ReorderConfig.defaultConfig();
        this.status = status != null ? status : InventoryStatus.ACTIVE;
        this.lastUpdated = lastUpdated;
    }

    public static InventoryItem create(
            Id id,
            String sku,
            Id merchantId,
            Id productVariantId,
            Id locationId,
            int initialQuantity,
            ReorderConfig reorderConfig
    ) {
        ReorderConfig config = reorderConfig != null ? reorderConfig : ReorderConfig.defaultConfig();
        if (config.wouldExceedMaxStock(0, initialQuantity)) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.ExceedsMaxStock(config.maxStock(), 0, initialQuantity),
                    "Initial quantity " + initialQuantity + " would exceed max stock " + config.maxStock()
            );
        }

        InventoryItem item = new InventoryItem(
                id, sku, merchantId, productVariantId, locationId,
                InventoryQuantity.withOnHand(initialQuantity),
                config,
                InventoryStatus.ACTIVE,
                LocalDateTime.now()
        );

        if (initialQuantity > 0) {
            item.addEvent(new StockReceivedEvent(id, sku, initialQuantity, locationId, LocalDateTime.now()));
        }
        item.checkAndRaiseLowStockAlert();

        return item;
    }

    public int getAvailableQuantity() {
        return quantity.available();
    }

    public StockMovement receiveStock(int qty, StockMovementType type, String referenceId, String notes, Id userId, Id movementId) {
        validateNotBlocked();
        validateReceiveType(type);
        validatePositiveQuantity(qty);
        validateDoesNotExceedMaxStock(quantity.onHand(), qty);

        int before = quantity.onHand();
        this.quantity = quantity.addOnHand(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), type, qty, before, before, quantity.reserved(), referenceId, userId
        );

        addEvent(new StockReceivedEvent(getId(), sku, qty, locationId, LocalDateTime.now()));
        checkAndRaiseLowStockAlert();
        updateStatusBasedOnQuantity();

        return movement;
    }

    public StockMovement reserveStock(int qty, String orderId, Id userId, Id movementId) {
        validateNotBlocked();
        validatePositiveQuantity(qty);
        if (qty > getAvailableQuantity()) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InsufficientQuantity(getAvailableQuantity(), qty),
                    "Cannot reserve " + qty + " units. Only " + getAvailableQuantity() + " available.");
        }

        int before = quantity.onHand();
        this.quantity = quantity.reserve(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.RESERVATION, qty, before, before, quantity.reserved() - qty, orderId, userId
        );

        addEvent(new StockReservedEvent(getId(), sku, qty, orderId, LocalDateTime.now()));
        checkAndRaiseLowStockAlert();
        return movement;
    }

    public StockMovement releaseReservation(int qty, String orderId, Id userId, Id movementId) {
        validateNotBlocked();
        validatePositiveQuantity(qty);
        if (qty > quantity.reserved()) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InsufficientQuantity(quantity.reserved(), qty),
                    "Cannot release " + qty + " units. Only " + quantity.reserved() + " reserved.");
        }

        int before = quantity.onHand();
        this.quantity = quantity.releaseReservation(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.RESERVATION_RELEASE, qty, before, before, quantity.reserved() + qty, orderId, userId
        );

        checkAndRaiseLowStockAlert();

        return movement;
    }

    public StockMovement shipStock(int qty, String orderId, Id userId, Id movementId) {
        validateNotBlocked();
        validatePositiveQuantity(qty);
        if (qty > quantity.reserved()) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InsufficientQuantity(quantity.reserved(), qty),
                    "Cannot ship " + qty + " units. Only " + quantity.reserved() + " reserved.");
        }

        int before = quantity.onHand();
        this.quantity = quantity.shipReserved(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.SALE, qty, before, before, quantity.reserved() + qty, orderId, userId
        );

        addEvent(new StockShippedEvent(getId(), sku, qty, orderId, LocalDateTime.now()));
        updateStatusBasedOnQuantity();

        return movement;
    }

    public StockMovement adjustStock(int newOnHandQuantity, AdjustmentReason reason, String notes, Id userId, Id movementId) {
        validateNotBlocked();
        int before = quantity.onHand();
        if (newOnHandQuantity > before) {
            validateDoesNotExceedMaxStock(0, newOnHandQuantity);
        }

        int adjustment = newOnHandQuantity - before;

        this.quantity = new InventoryQuantity(
                newOnHandQuantity,
                quantity.reserved(),
                quantity.inTransit(),
                quantity.damaged()
        );
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.CYCLE_COUNT_ADJUSTMENT, adjustment, before,
                before, quantity.reserved(),
                reason.name(), userId
        );

        addEvent(new StockAdjustedEvent(getId(), sku, before, newOnHandQuantity, reason, LocalDateTime.now()));
        checkAndRaiseLowStockAlert();
        updateStatusBasedOnQuantity();

        return movement;
    }

    public StockMovement markDamaged(int qty, String notes, Id userId, Id movementId) {
        validateNotBlocked();
        validatePositiveQuantity(qty);

        int before = quantity.onHand();
        this.quantity = quantity.markDamaged(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.DAMAGE_ADJUSTMENT, -qty, before,
                before, quantity.reserved(),
                AdjustmentReason.DAMAGED.name(), userId
        );

        addEvent(new StockAdjustedEvent(getId(), sku, before, quantity.onHand(), AdjustmentReason.DAMAGED, LocalDateTime.now()));
        checkAndRaiseLowStockAlert();
        updateStatusBasedOnQuantity();

        return movement;
    }

    public StockMovement writeOff(int qty, String reason, String notes, Id userId, Id movementId) {
        validateNotBlocked();
        validatePositiveQuantity(qty);
        if (qty > getAvailableQuantity()) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InsufficientQuantity(getAvailableQuantity(), qty),
                    "Cannot write off " + qty + " units. Only " + getAvailableQuantity() + " available.");
        }

        int before = quantity.onHand();
        this.quantity = quantity.subtractOnHand(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.WRITE_OFF, qty, before, before, quantity.reserved(), reason, userId
        );

        checkAndRaiseLowStockAlert();
        updateStatusBasedOnQuantity();

        return movement;
    }

    public StockMovement transferOut(int qty, String transferId, Id userId, Id movementId) {
        validateNotBlocked();
        validatePositiveQuantity(qty);
        if (qty > getAvailableQuantity()) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InsufficientQuantity(getAvailableQuantity(), qty),
                    "Cannot transfer out " + qty + " units. Only " + getAvailableQuantity() + " available.");
        }

        int before = quantity.onHand();
        this.quantity = quantity.subtractOnHand(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.TRANSFER_OUT, qty, before, before, quantity.reserved(), transferId, userId
        );

        checkAndRaiseLowStockAlert();
        updateStatusBasedOnQuantity();

        return movement;
    }

    public StockMovement returnToVendor(int qty, String reason, String notes, Id userId, Id movementId) {
        validateNotBlocked();
        validatePositiveQuantity(qty);
        if (qty > getAvailableQuantity()) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InsufficientQuantity(getAvailableQuantity(), qty),
                    "Cannot return to vendor " + qty + " units. Only " + getAvailableQuantity() + " available.");
        }

        int before = quantity.onHand();
        this.quantity = quantity.subtractOnHand(qty);
        this.lastUpdated = LocalDateTime.now();

        StockMovement movement = StockMovement.create(
                movementId, getId(), StockMovementType.RETURN_TO_VENDOR, qty, before, before, quantity.reserved(), reason, userId
        );

        checkAndRaiseLowStockAlert();
        updateStatusBasedOnQuantity();

        return movement;
    }

    public void discontinue() {
        this.status = InventoryStatus.DISCONTINUED;
        this.lastUpdated = LocalDateTime.now();
        addEvent(new InventoryItemDiscontinuedEvent(
                getId(),
                sku,
                productVariantId != null ? productVariantId.getValue() : null,
                locationId,
                LocalDateTime.now()
        ));
    }

    public void suspend() {
        this.status = InventoryStatus.SUSPENDED;
        this.lastUpdated = LocalDateTime.now();
    }

    public void activate() {
        this.status = InventoryStatus.ACTIVE;
        this.lastUpdated = LocalDateTime.now();
        updateStatusBasedOnQuantity();
    }

    public boolean isLowStock() {
        return reorderConfig.isLowStock(getAvailableQuantity());
    }

    public boolean needsReorder() {
        return reorderConfig.needsReorder(getAvailableQuantity());
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }

    public boolean isActive() {
        return status == InventoryStatus.ACTIVE;
    }

    public boolean canFulfill(int qty) {
        return isActive() && getAvailableQuantity() >= qty;
    }

    public int getSuggestedReorderQuantity() {
        return reorderConfig.suggestedOrderQuantity(getAvailableQuantity());
    }

    private void validateReceiveType(StockMovementType type) {
        if (type != StockMovementType.PURCHASE_ORDER_RECEIPT
                && type != StockMovementType.CUSTOMER_RETURN
                && type != StockMovementType.TRANSFER_IN
                && type != StockMovementType.INITIAL_STOCK) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidStockMovementType(type.name()),
                    "Invalid Stock Movement Type"
            );
        }
    }

    private void validateNotBlocked() {
        if (status == InventoryStatus.SUSPENDED || status == InventoryStatus.DISCONTINUED) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.StockOperationBlocked(status.name(), sku),
                    "Stock operations are blocked on " + status + " inventory item: " + sku
            );
        }
    }

    private void validatePositiveQuantity(int qty) {
        if (qty < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.NegativeQuantity(qty),
                    "Qty cannot be negative"
            );
        }
    }

    private void validateDoesNotExceedMaxStock(int currentOnHand, int incomingQuantity) {
        if (reorderConfig.wouldExceedMaxStock(currentOnHand, incomingQuantity)) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.ExceedsMaxStock(
                            reorderConfig.maxStock(),
                            currentOnHand,
                            incomingQuantity
                    ),
                    "Quantity would exceed max stock " + reorderConfig.maxStock()
            );
        }
    }

    private void checkAndRaiseLowStockAlert() {
        if (isLowStock() && isActive()) {
            addEvent(new LowStockAlertEvent(
                    getId(), sku, getAvailableQuantity(),
                    reorderConfig.reorderPoint(), LocalDateTime.now()
            ));
        }
    }

    private void updateStatusBasedOnQuantity() {
        if (status == InventoryStatus.ACTIVE && isOutOfStock()) {
            this.status = InventoryStatus.OUT_OF_STOCK;
        } else if (status == InventoryStatus.OUT_OF_STOCK && !isOutOfStock()) {
            this.status = InventoryStatus.ACTIVE;
        }
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + getId().getValue() +
                ", sku='" + sku + '\'' +
                ", merchantId=" + merchantId.getValue() +
                ", locationId=" + locationId.getValue() +
                ", available=" + getAvailableQuantity() +
                ", onHand=" + quantity.onHand() +
                ", reserved=" + quantity.reserved() +
                ", status=" + status +
                '}';
    }
}
