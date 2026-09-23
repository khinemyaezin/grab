package com.customer.domain.aggregate;

import com.customer.domain.entity.CustomerAddress;
import com.customer.domain.enums.CustomerStatus;
import com.customer.domain.event.CustomerAttachedToUserEvent;
import com.customer.domain.event.CustomerRegisteredEvent;
import com.customer.domain.event.CustomerSuspendedEvent;
import com.customer.domain.event.GuestCustomerCreatedEvent;
import com.customer.domain.exception.CustomerDomainError;
import com.customer.domain.exception.CustomerDomainException;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
public class Customer extends AggregateRoot<Id> {
    private Id userId;
    private String email;
    private String displayName;
    private CustomerStatus status;
    private final List<CustomerAddress> addresses;
    private final Instant createdAt;
    private Instant updatedAt;

    public Customer(
            Id id,
            Id userId,
            String email,
            String displayName,
            CustomerStatus status,
            List<CustomerAddress> addresses,
            Instant createdAt,
            Instant updatedAt
    ) {
        super(id);
        this.userId = userId;
        this.email = Objects.requireNonNull(email, "email is required");
        this.displayName = displayName;
        this.status = Objects.requireNonNull(status, "status is required");
        this.addresses = addresses == null ? new ArrayList<>() : new ArrayList<>(addresses);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static Customer register(Id id, Id userId, String email, String displayName, Instant now) {
        Objects.requireNonNull(userId, "userId is required");
        Customer customer = new Customer(
                id,
                userId,
                email,
                displayName,
                CustomerStatus.ACTIVE,
                new ArrayList<>(),
                now,
                now
        );
        customer.addEvent(new CustomerRegisteredEvent(
                id.getValue(),
                userId.getValue(),
                email,
                displayName,
                now
        ));
        return customer;
    }

    public static Customer createGuest(Id id, String email, String displayName, Instant now) {
        Customer customer = new Customer(
                id,
                null,
                email,
                displayName,
                CustomerStatus.ACTIVE,
                new ArrayList<>(),
                now,
                now
        );
        customer.addEvent(new GuestCustomerCreatedEvent(id.getValue(), email, displayName, now));
        return customer;
    }

    public void attachUser(Id userId, Instant now) {
        if (this.userId != null) {
            throw new CustomerDomainException(
                    new CustomerDomainError.UserAlreadyAttached(getId().getValue()),
                    "Customer already has a linked user"
            );
        }
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.updatedAt = now;
        addEvent(new CustomerAttachedToUserEvent(getId().getValue(), userId.getValue(), now));
    }

    public void suspend(Instant now) {
        if (status == CustomerStatus.SUSPENDED) {
            return;
        }
        this.status = CustomerStatus.SUSPENDED;
        this.updatedAt = now;
        addEvent(new CustomerSuspendedEvent(getId().getValue(), now));
    }

    public Optional<Id> getUserIdOptional() {
        return Optional.ofNullable(userId);
    }
}
