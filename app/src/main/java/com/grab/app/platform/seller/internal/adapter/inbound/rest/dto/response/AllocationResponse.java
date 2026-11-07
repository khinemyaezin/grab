package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.time.LocalDateTime;

public record AllocationResponse(
        String reservationId,
        String orderId,
        String sku,
        String locationId,
        int quantity,
        String status,
        LocalDateTime expiresAt
) {}
