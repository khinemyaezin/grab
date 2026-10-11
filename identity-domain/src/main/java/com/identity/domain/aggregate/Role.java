package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.event.RoleAuthorityChangedEvent;
import com.identity.domain.event.RoleCreatedEvent;
import com.identity.domain.event.RoleDetailsUpdatedEvent;
import com.identity.domain.event.RoleStatusChangedEvent;
import com.identity.domain.enums.RoleKind;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Role extends AggregateRoot<Id> {
    private final String code;
    private String name;
    private String description;
    private final RoleKind kind;
    private boolean active;
    private boolean assignable;
    private final Set<Authority> authorities;

    public Role(Id id, String code, String name, String description, boolean active, Set<Authority> authorities) {
        this(id, code, name, description, RoleKind.CUSTOM, active, true, authorities);
    }

    public Role(
            Id id,
            String code,
            String name,
            String description,
            RoleKind kind,
            boolean active,
            boolean assignable,
            Set<Authority> authorities
    ) {
        super(id);
        this.code = normalizeRoleCode(code);
        this.name = validateName(name);
        this.description = description;
        this.kind = Objects.requireNonNull(kind, "role kind is required");
        this.active = active;
        this.assignable = assignable;
        this.authorities = normalizeAuthorities(authorities);
    }

    public static Role createCustom(
            Id id,
            String code,
            String name,
            String description,
            Set<Authority> authorities
    ) {
        Set<Authority> normalizedAuthorities = normalizeAuthorities(authorities);
        if (normalizedAuthorities.isEmpty()) {
            throw authoritiesRequired();
        }
        Role role = new Role(
                id,
                code,
                name,
                description,
                RoleKind.CUSTOM,
                true,
                true,
                normalizedAuthorities
        );
        role.addEvent(new RoleCreatedEvent(
                id, role.code, role.name, description, role.active, role.authorityCodes(), LocalDateTime.now()
        ));
        return role;
    }

    public static Role createSystemPending(Id id, String code, String name, String description) {
        Role role = new Role(id, code, name, description, RoleKind.SYSTEM, false, false, Set.of());
        role.addEvent(new RoleCreatedEvent(id, role.code, role.name, description, false, Set.of(), LocalDateTime.now()));
        return role;
    }

    public static Role rehydrate(
            Id id,
            String code,
            String name,
            String description,
            RoleKind kind,
            boolean active,
            boolean assignable,
            Set<Authority> authorities
    ) {
        return new Role(id, code, name, description, kind, active, assignable, authorities);
    }

    public void updateDetails(String name, String description) {
        requireCustomRole();
        this.name = validateName(name);
        this.description = description;
        addEvent(new RoleDetailsUpdatedEvent(getId(), this.name, description, LocalDateTime.now()));
    }

    public void activate() {
        requireCustomRole();
        if (!active) {
            active = true;
            addEvent(new RoleStatusChangedEvent(getId(), true, LocalDateTime.now()));
        }
    }

    public void deactivate() {
        requireCustomRole();
        if (active) {
            active = false;
            addEvent(new RoleStatusChangedEvent(getId(), false, LocalDateTime.now()));
        }
    }

    public void assignAuthority(Authority authority) {
        requireCustomRole();
        Authority requiredAuthority = Objects.requireNonNull(authority, "authority is required");
        if (authorities.add(requiredAuthority)) {
            addEvent(new RoleAuthorityChangedEvent(getId(), requiredAuthority.getCode(), true, LocalDateTime.now()));
        }
    }

    public void revokeAuthority(Authority authority) {
        requireCustomRole();
        Authority requiredAuthority = Objects.requireNonNull(authority, "authority is required");
        if (authorities.contains(requiredAuthority) && authorities.size() == 1) {
            throw authoritiesRequired();
        }
        if (authorities.remove(requiredAuthority)) {
            addEvent(new RoleAuthorityChangedEvent(getId(), requiredAuthority.getCode(), false, LocalDateTime.now()));
        }
    }

    public void reconcileSystemAuthorities(
            Set<Authority> reconciledAuthorities
    ) {
        if (this.kind != RoleKind.SYSTEM) {
            throw new IllegalStateException("reconcileSystemAuthorities only applies to SYSTEM roles");
        }
        Set<Authority> nextAuthorities = normalizeAuthorities(reconciledAuthorities);
        Set<String> currentCodes = this.authorities.stream().map(Authority::getCode).collect(Collectors.toSet());
        Set<String> nextCodes = nextAuthorities.stream().map(Authority::getCode).collect(Collectors.toSet());
        this.authorities.stream()
                .filter(authority -> !nextCodes.contains(authority.getCode()))
                .forEach(authority -> addEvent(new RoleAuthorityChangedEvent(
                        getId(), authority.getCode(), false, LocalDateTime.now())));
        nextAuthorities.stream()
                .filter(authority -> !currentCodes.contains(authority.getCode()))
                .forEach(authority -> addEvent(new RoleAuthorityChangedEvent(
                        getId(), authority.getCode(), true, LocalDateTime.now())));
        this.authorities.clear();
        this.authorities.addAll(nextAuthorities);
        if (!this.active) {
            addEvent(new RoleStatusChangedEvent(getId(), true, LocalDateTime.now()));
        }
        this.assignable = true;
        this.active = true;
    }

    public void suspendUntilDeclared(boolean hasAppliedDeclaration) {
        if (this.kind != RoleKind.SYSTEM) {
            throw new IllegalStateException("Only SYSTEM roles can be suspended by a declaration");
        }
        if (!hasAppliedDeclaration) {
            boolean wasActive = this.active;
            this.active = false;
            this.assignable = false;
            if (wasActive) {
                addEvent(new RoleStatusChangedEvent(getId(), false, LocalDateTime.now()));
            }
        }
    }

    public Set<Authority> getAuthorities() {
        return Set.copyOf(authorities);
    }

    public void requireAssignable() {
        if (!active || !assignable) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.RoleNotAssignable(code),
                    "Role is not available for new assignments"
            );
        }
    }

    private void requireCustomRole() {
        if (kind == RoleKind.SYSTEM) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.SystemRoleModificationForbidden(code),
                    "System roles can only be changed through a release migration"
            );
        }
    }

    private Set<String> authorityCodes() {
        return authorities.stream().map(Authority::getCode).collect(Collectors.toUnmodifiableSet());
    }

    private static LinkedHashSet<Authority> normalizeAuthorities(Set<Authority> values) {
        Objects.requireNonNull(values, "authorities are required");
        LinkedHashSet<Authority> normalizedAuthorities = new LinkedHashSet<>();
        values.forEach(authority -> normalizedAuthorities.add(Objects.requireNonNull(authority, "authority is required")));
        return normalizedAuthorities;
    }

    private static String normalizeRoleCode(String value) {
        if (value == null) {
            throw invalidRoleCode(null);
        }
        String normalized = normalizeCode(value);
        if (normalized == null) {
            throw invalidRoleCode(value);
        }
        return normalized;
    }

    private static String normalizeCode(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return normalized.matches("[A-Z][A-Z0-9_]*") ? normalized : null;
    }

    private static String validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidRoleName(),
                    "Role name is required"
            );
        }
        return value.trim();
    }

    private static IdentityDomainValidationException invalidRoleCode(String code) {
        return new IdentityDomainValidationException(
                new IdentityDomainError.InvalidRoleCode(String.valueOf(code)),
                "Invalid role code"
        );
    }

    private static IdentityDomainValidationException authoritiesRequired() {
        return new IdentityDomainValidationException(
                new IdentityDomainError.RoleAuthoritiesRequired(),
                "A custom role requires at least one authority"
        );
    }
}
