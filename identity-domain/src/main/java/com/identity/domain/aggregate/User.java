package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.enums.UserStatus;
import com.identity.domain.valueobject.Email;
import com.identity.domain.valueobject.HashedPassword;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
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
        this.roleCodes = new LinkedHashSet<>(Objects.requireNonNull(roleCodes));
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static User createLocal(Id id, Email email, HashedPassword password, String roleCode) {
        if (!Set.of("CUSTOMER", "SELLER").contains(roleCode)) {
            throw new IllegalArgumentException("only CUSTOMER or SELLER can self-register");
        }
        LocalDateTime now = LocalDateTime.now();
        UserStatus status = "SELLER".equals(roleCode) ? UserStatus.PENDING_APPROVAL : UserStatus.ACTIVE;
        return new User(id, email, Objects.requireNonNull(password), Set.of(roleCode), status, now, now);
    }

    public void activate() {
        if (status != UserStatus.PENDING_APPROVAL) throw new IllegalStateException("user is not pending approval");
        status = UserStatus.ACTIVE;
        updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        if (status == UserStatus.SUSPENDED) throw new IllegalStateException("user is already suspended");
        status = UserStatus.SUSPENDED;
        updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        if (status != UserStatus.SUSPENDED) throw new IllegalStateException("user is not suspended");
        status = UserStatus.ACTIVE;
        updatedAt = LocalDateTime.now();
    }

    public void assignRole(String roleCode) {
        roleCodes.add(Objects.requireNonNull(roleCode));
        updatedAt = LocalDateTime.now();
    }

    public void revokeRole(String roleCode) {
        roleCodes.remove(roleCode);
        updatedAt = LocalDateTime.now();
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
}
