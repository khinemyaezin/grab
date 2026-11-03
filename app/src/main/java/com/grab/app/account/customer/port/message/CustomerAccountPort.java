package com.grab.app.account.customer.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.Optional;

/**
 * Universal Java Port Interface for Customer Profile.
 */
public interface CustomerAccountPort {

    Optional<CustomerProfileResult> getCustomerById(Id customerId);

    Optional<CustomerProfileResult> getCustomerByUserId(Id userId);

    record CustomerProfileResult(
            String id,
            String userId,
            String email,
            String displayName,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
