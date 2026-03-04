package com.inventory.domain.valueobject;

public record InventoryQuantity(
        int onHand,
        int reserved,
        int inTransit,
        int damaged
) {

    public InventoryQuantity {
        if (onHand < 0) throw new IllegalArgumentException("OnHand quantity cannot be negative");
        if (reserved < 0) throw new IllegalArgumentException("Reserved quantity cannot be negative");
        if (inTransit < 0) throw new IllegalArgumentException("InTransit quantity cannot be negative");
        if (damaged < 0) throw new IllegalArgumentException("Damaged quantity cannot be negative");
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
            throw new IllegalArgumentException("Cannot subtract more than available on hand");
        }
        return new InventoryQuantity(newOnHand, reserved, inTransit, damaged);
    }

    public InventoryQuantity reserve(int quantity) {
        if (quantity > available()) {
            throw new IllegalArgumentException("Cannot reserve more than available quantity");
        }
        return new InventoryQuantity(onHand, reserved + quantity, inTransit, damaged);
    }

    public InventoryQuantity releaseReservation(int quantity) {
        int newReserved = reserved - quantity;
        if (newReserved < 0) {
            throw new IllegalArgumentException("Cannot release more than reserved quantity");
        }
        return new InventoryQuantity(onHand, newReserved, inTransit, damaged);
    }

    public InventoryQuantity addInTransit(int quantity) {
        return new InventoryQuantity(onHand, reserved, inTransit + quantity, damaged);
    }

    public InventoryQuantity receiveInTransit(int quantity) {
        int newInTransit = inTransit - quantity;
        if (newInTransit < 0) {
            throw new IllegalArgumentException("Cannot receive more than in transit quantity");
        }
        return new InventoryQuantity(onHand + quantity, reserved, newInTransit, damaged);
    }

    public InventoryQuantity markDamaged(int quantity) {
        if (quantity > onHand - damaged) {
            throw new IllegalArgumentException("Cannot mark more as damaged than available undamaged stock");
        }
        return new InventoryQuantity(onHand, reserved, inTransit, damaged + quantity);
    }

    public InventoryQuantity shipReserved(int quantity) {
        if (quantity > reserved) {
            throw new IllegalArgumentException("Cannot ship more than reserved quantity");
        }
        return new InventoryQuantity(onHand - quantity, reserved - quantity, inTransit, damaged);
    }
}
