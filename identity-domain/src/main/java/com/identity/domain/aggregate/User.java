package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.event.UserRegisteredEvent;
import com.identity.domain.event.UserStatusChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Getter
public class User extends AggregateRoot<Id> {
    private final Email email;
    private final HashedPassword passwordHash;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(Id id, Email email, HashedPassword passwordHash,
                UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = passwordHash;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static User createLocal(Id id, Email email, HashedPassword password) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(id, email, Objects.requireNonNull(password), UserStatus.ACTIVE, now, now);
        user.addEvent(new UserRegisteredEvent(id, email.value(), UserStatus.ACTIVE, now));
        return user;
    }

    public void activate() {
        changeStatus(UserStatus.PENDING_APPROVAL, UserStatus.ACTIVE);
    }

    public void suspend() {
        if (status == UserStatus.SUSPENDED) {
            throw invalidStatusTransition(UserStatus.SUSPENDED);
        }
        setStatus(UserStatus.SUSPENDED);
    }

    public void reactivate() {
        changeStatus(UserStatus.SUSPENDED, UserStatus.ACTIVE);
    }

    public Optional<HashedPassword> getPasswordHash() {
        return Optional.ofNullable(passwordHash);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    private void changeStatus(UserStatus requiredCurrentStatus, UserStatus requestedStatus) {
        if (status != requiredCurrentStatus) {
            throw invalidStatusTransition(requestedStatus);
        }
        setStatus(requestedStatus);
    }

    private void setStatus(UserStatus requestedStatus) {
        UserStatus previousStatus = status;
        status = requestedStatus;
        updatedAt = LocalDateTime.now();
        addEvent(new UserStatusChangedEvent(getId(), previousStatus, requestedStatus, updatedAt));
    }

    private IdentityDomainValidationException invalidStatusTransition(UserStatus requestedStatus) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidUserStatusTransition(status.name(), requestedStatus.name()),
                "User status cannot transition from " + status + " to " + requestedStatus
        );
    }

}
