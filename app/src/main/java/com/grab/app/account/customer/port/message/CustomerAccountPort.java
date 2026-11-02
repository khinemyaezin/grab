package com.grab.app.account.customer.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Customer Profile and Address Book.
 */
public interface CustomerAccountPort {

    CustomerProfileResult getCustomerProfile(Id customerId);

    CustomerProfileResult updateCustomerProfile(Id customerId, UpdateCustomerProfileMessage message);

    AddressResult addAddress(Id customerId, AddAddressMessage message);

    void removeAddress(Id customerId, Id addressId);

    List<AddressResult> listAddresses(Id customerId);

    record UpdateCustomerProfileMessage(
            String firstName,
            String lastName,
            String phoneNumber
    ) {}

    record AddAddressMessage(
            String addressLine1,
            String city,
            String postalCode,
            String countryCode,
            boolean defaultShipping
    ) {}

    record CustomerProfileResult(
            Id id,
            Id userId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            Instant createdAt
    ) {}

    record AddressResult(
            Id id,
            Id customerId,
            String addressLine1,
            String city,
            String postalCode,
            String countryCode,
            boolean defaultShipping
    ) {}
}

