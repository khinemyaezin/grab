package com.customer.application.model.read;

import com.customer.domain.enums.CustomerStatus;

import java.time.Instant;

public record CustomerView(
        String id,
        String userId,
        String email,
        String displayName,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
