package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.event.UserRegisteredEvent;
import com.identity.domain.event.UserRoleChangedEvent;
import com.identity.domain.event.UserStatusChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
public class User extends AggregateRoot<Id> {
    private final Email email;
    private final HashedPassword passwordHash;
    private final Set<String> roleCodes;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(Id id, Email email, HashedPassword passwordHash, Set<String> roleCodes,
                UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = passwordHash;
        this.roleCodes = normalizeRoleCodes(roleCodes);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static User createLocal(Id id, Email email, HashedPassword password, String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (!Set.of("CUSTOMER", "SELLER").contains(normalizedRoleCode)) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidSelfRegistrationRole(normalizedRoleCode),
                    "Only CUSTOMER or SELLER can self-register"
            );
        }
        LocalDateTime now = LocalDateTime.now();
        UserStatus status = "SELLER".equals(normalizedRoleCode) ? UserStatus.PENDING_APPROVAL : UserStatus.ACTIVE;
        User user = new User(id, email, Objects.requireNonNull(password), Set.of(normalizedRoleCode), status, now, now);
        user.addEvent(new UserRegisteredEvent(id, email.value(), user.roleCodes, status, now));
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

    public void assignRole(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (roleCodes.add(normalizedRoleCode)) {
            updatedAt = LocalDateTime.now();
            addEvent(new UserRoleChangedEvent(getId(), normalizedRoleCode, true, updatedAt));
        }
    }

    public void revokeRole(String roleCode) {
        String normalizedRoleCode = normalizeRoleCode(roleCode);
        if (roleCodes.remove(normalizedRoleCode)) {
            updatedAt = LocalDateTime.now();
            addEvent(new UserRoleChangedEvent(getId(), normalizedRoleCode, false, updatedAt));
        }
    }

    public Optional<HashedPassword> getPasswordHash() {
        return Optional.ofNullable(passwordHash);
    }

    public Set<String> getRoleCodes() {
        return Set.copyOf(roleCodes);
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

    private static LinkedHashSet<String> normalizeRoleCodes(Set<String> roleCodes) {
        Objects.requireNonNull(roleCodes, "roleCodes are required");
        LinkedHashSet<String> normalizedCodes = new LinkedHashSet<>();
        roleCodes.forEach(roleCode -> normalizedCodes.add(normalizeRoleCode(roleCode)));
        return normalizedCodes;
    }

    private static String normalizeRoleCode(String roleCode) {
        if (roleCode == null) {
            throw invalidRoleCode(null);
        }
        String normalizedRoleCode = roleCode.trim().toUpperCase(Locale.ROOT);
        if (!normalizedRoleCode.matches("[A-Z][A-Z0-9_]*")) {
            throw invalidRoleCode(roleCode);
        }
        return normalizedRoleCode;
    }

    private static IdentityDomainValidationException invalidRoleCode(String roleCode) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidRoleCode(String.valueOf(roleCode)),
                "Invalid role code"
        );
    }
}
