package com.inventory.application.model.write;

public record ExpireExpiredReservationsResult(
        int scanned,
        int expired
) {
}
