package com.identity.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.identity.domain.event.RoleAuthorityChangedEvent;
import com.identity.domain.event.RoleCreatedEvent;
import com.identity.domain.event.RoleDetailsUpdatedEvent;
import com.identity.domain.event.RoleStatusChangedEvent;
import com.identity.domain.exception.IdentityDomainError;
import com.identity.domain.exception.IdentityDomainValidationException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Getter
public class Role extends AggregateRoot<Id> {
    private final String code;
    private String name;
    private String description;
    private boolean active;
    private final Set<String> authorityCodes;

    public Role(Id id, String code, String name, String description, boolean active, Set<String> authorityCodes) {
        super(id);
        this.code = normalizeRoleCode(code);
        this.name = validateName(name);
        this.description = description;
        this.active = active;
        this.authorityCodes = normalizeCodes(authorityCodes);
    }

    public static Role create(Id id, String code, String name, String description) {
        Role role = new Role(id, code, name, description, true, Set.of());
        role.addEvent(new RoleCreatedEvent(
                id, role.code, role.name, description, role.active, role.authorityCodes, LocalDateTime.now()
        ));
        return role;
    }

    public void updateDetails(String name, String description) {
        this.name = validateName(name);
        this.description = description;
        addEvent(new RoleDetailsUpdatedEvent(getId(), this.name, description, LocalDateTime.now()));
    }

    public void activate() {
        if (!active) {
            active = true;
            addEvent(new RoleStatusChangedEvent(getId(), true, LocalDateTime.now()));
        }
    }

    public void deactivate() {
        if (active) {
            active = false;
            addEvent(new RoleStatusChangedEvent(getId(), false, LocalDateTime.now()));
        }
    }

    public void assignAuthority(String code) {
        String normalizedCode = normalizeAuthorityCode(code);
        if (authorityCodes.add(normalizedCode)) {
            addEvent(new RoleAuthorityChangedEvent(getId(), normalizedCode, true, LocalDateTime.now()));
        }
    }

    public void revokeAuthority(String code) {
        String normalizedCode = normalizeAuthorityCode(code);
        if (authorityCodes.remove(normalizedCode)) {
            addEvent(new RoleAuthorityChangedEvent(getId(), normalizedCode, false, LocalDateTime.now()));
        }
    }

    public Set<String> getAuthorityCodes() {
        return Set.copyOf(authorityCodes);
    }

    private static LinkedHashSet<String> normalizeCodes(Set<String> codes) {
        Objects.requireNonNull(codes, "authorityCodes are required");
        LinkedHashSet<String> normalizedCodes = new LinkedHashSet<>();
        codes.forEach(code -> normalizedCodes.add(normalizeAuthorityCode(code)));
        return normalizedCodes;
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

    private static String normalizeAuthorityCode(String value) {
        String normalized = normalizeCode(value);
        if (normalized == null) {
            throw new IdentityDomainValidationException(
                    new IdentityDomainError.InvalidAuthorityCode(String.valueOf(value)),
                    "Invalid authority code"
            );
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
}
