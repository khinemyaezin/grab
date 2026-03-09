package com.inventory.domain.valueobject;

import com.inventory.domain.exception.InventoryDomainError;
import com.inventory.domain.exception.InventoryDomainValidationException;

public record InventoryQuantity(
        int onHand,
        int reserved,
        int inTransit,
        int damaged
) {

    public InventoryQuantity {
        if (onHand < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidOnHandQuantity(onHand),
                    "OnHand quantity cannot be negative"
            );
        }
        if (reserved < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidReservedQuantity(reserved),
                    "Reserved quantity cannot be negative"
            );
        }
        if (inTransit < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidInTransitQuantity(inTransit),
                    "InTransit quantity cannot be negative"
            );
        }
        if (damaged < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidDamagedQuantity(damaged),
                    "Damaged quantity cannot be negative"
            );
        }
    }

    public static InventoryQuantity zero() {
        return new InventoryQuantity(0, 0, 0, 0);
    }

    public static InventoryQuantity withOnHand(int onHand) {
        return new InventoryQuantity(onHand, 0, 0, 0);
    }

    public int available() {
        return Math.max(0, onHand - reserved - damaged);
    }

    public int total() {
        return onHand + inTransit;
    }

    public int sellable() {
        return Math.max(0, onHand - damaged);
    }

    public InventoryQuantity addOnHand(int quantity) {
        return new InventoryQuantity(onHand + quantity, reserved, inTransit, damaged);
    }

    public InventoryQuantity subtractOnHand(int quantity) {
        int newOnHand = onHand - quantity;
        if (newOnHand < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.SubtractExceedsOnHand(onHand, quantity),
                    "Cannot subtract more than available on hand"
            );
        }
        return new InventoryQuantity(newOnHand, reserved, inTransit, damaged);
    }

    public InventoryQuantity reserve(int quantity) {
        if (quantity > available()) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.ReserveExceedsAvailable(available(), quantity),
                    "Cannot reserve more than available quantity"
            );
        }
        return new InventoryQuantity(onHand, reserved + quantity, inTransit, damaged);
    }

    public InventoryQuantity releaseReservation(int quantity) {
        int newReserved = reserved - quantity;
        if (newReserved < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.ReleaseExceedsReserved(reserved, quantity),
                    "Cannot release more than reserved quantity"
            );
        }
        return new InventoryQuantity(onHand, newReserved, inTransit, damaged);
    }

    public InventoryQuantity addInTransit(int quantity) {
        return new InventoryQuantity(onHand, reserved, inTransit + quantity, damaged);
    }

    public InventoryQuantity receiveInTransit(int quantity) {
        int newInTransit = inTransit - quantity;
        if (newInTransit < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.ReceiveExceedsInTransit(inTransit, quantity),
                    "Cannot receive more than in transit quantity"
            );
        }
        return new InventoryQuantity(onHand + quantity, reserved, newInTransit, damaged);
    }

    public InventoryQuantity markDamaged(int quantity) {
        int undamaged = onHand - damaged;
        if (quantity > undamaged) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.DamageExceedsUndamaged(undamaged, quantity),
                    "Cannot mark more as damaged than available undamaged stock"
            );
        }
        return new InventoryQuantity(onHand, reserved, inTransit, damaged + quantity);
    }

    public InventoryQuantity shipReserved(int quantity) {
        if (quantity > reserved) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.ShipExceedsReserved(reserved, quantity),
                    "Cannot ship more than reserved quantity"
            );
        }
        return new InventoryQuantity(onHand - quantity, reserved - quantity, inTransit, damaged);
    }
}
