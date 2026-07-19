package com.grab.store.inventory.internal.command;

public record ExpireExpiredReservationsResult(
        int scanned,
        int expired
) {
}
